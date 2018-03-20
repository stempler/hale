/*
 * Copyright (c) 2017 wetransform GmbH
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     wetransform GmbH <http://www.wetransform.to>
 */

package eu.esdihumboldt.hale.common.align.merge.impl;

import static eu.esdihumboldt.hale.common.align.migrate.util.MigrationUtil.isDirectMatch;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import eu.esdihumboldt.hale.common.align.merge.MergeCellMigrator;
import eu.esdihumboldt.hale.common.align.merge.MergeIndex;
import eu.esdihumboldt.hale.common.align.merge.MergeUtil;
import eu.esdihumboldt.hale.common.align.migrate.AlignmentMigration;
import eu.esdihumboldt.hale.common.align.migrate.CellMigrator;
import eu.esdihumboldt.hale.common.align.migrate.MigrationOptions;
import eu.esdihumboldt.hale.common.align.migrate.impl.DefaultCellMigrator;
import eu.esdihumboldt.hale.common.align.migrate.impl.MigrationOptionsImpl;
import eu.esdihumboldt.hale.common.align.model.AlignmentUtil;
import eu.esdihumboldt.hale.common.align.model.Cell;
import eu.esdihumboldt.hale.common.align.model.CellUtil;
import eu.esdihumboldt.hale.common.align.model.Entity;
import eu.esdihumboldt.hale.common.align.model.EntityDefinition;
import eu.esdihumboldt.hale.common.align.model.MutableCell;
import eu.esdihumboldt.hale.common.align.model.annotations.messages.CellLog;
import eu.esdihumboldt.hale.common.align.model.impl.DefaultCell;
import eu.esdihumboldt.hale.common.core.report.SimpleLog;
import eu.esdihumboldt.hale.common.schema.model.PropertyDefinition;
import eu.esdihumboldt.hale.common.schema.model.constraint.type.GeometryType;

/**
 * Cell merger base class.
 * 
 * @author Simon Templer
 * @param <C> the context class
 */
public abstract class AbstractMergeCellMigrator<C> extends DefaultCellMigrator
		implements MergeCellMigrator {

	@Override
	public Iterable<MutableCell> mergeCell(Cell originalCell, MergeIndex mergeIndex,
			AlignmentMigration migration, Function<String, CellMigrator> getCellMigrator,
			SimpleLog log) {
		MutableCell copy = new DefaultCell(originalCell);

		// update source entities
		if (copy.getSource() != null && !copy.getSource().isEmpty()) {
			ListMultimap<String, ? extends Entity> sources = copy.getSource();
			return mergeSources(sources, mergeIndex, originalCell, migration, getCellMigrator, log);
		}
		else {
			// no sources - return original
			return Collections.singleton(copy);
		}
	}

	/**
	 * Create a new context object.
	 * 
	 * @param originalCell the original cell
	 * 
	 * @return the new context object
	 */
	protected abstract C newContext(Cell originalCell);

	/**
	 * Update the cell sources.
	 * 
	 * @param sources the old sources
	 * @param mergeIndex the merge index
	 * @param originalCell the original cell
	 * @param migration the alignment migration (may be useful for cases where
	 *            only entity replacement needs to be done)
	 * @param getCellMigrator functions that yields a cell migrator for a
	 *            function (may be useful for cases where only entity
	 *            replacement needs to be done)
	 * @param log the migration process log
	 * @return the merged cell or cells
	 */
	protected Iterable<MutableCell> mergeSources(ListMultimap<String, ? extends Entity> sources,
			MergeIndex mergeIndex, Cell originalCell, AlignmentMigration migration,
			Function<String, CellMigrator> getCellMigrator, SimpleLog log) {
		boolean transferBase = true; // XXX relevant here at all?

		if (sources.size() == 1) {
			EntityDefinition source = sources.values().iterator().next().getDefinition();
			List<Cell> matches = mergeIndex.getCellsForTarget(source);

			List<String> storedMessages = new ArrayList<>();
			boolean inaccurateMatch = false;
			if (matches.isEmpty()) {
				// try to find match via parent (in specific cases)
				matches = findParentMatch(source, mergeIndex);
				if (!matches.isEmpty()) {
					inaccurateMatch = true;
					// message may not be added in every case, because it may be
					// a duplicate
//					storedMessages.add(MessageFormat
//							.format("Inaccurate match of {0} via parent entity", source));
				}
			}

			if (!matches.isEmpty()) {
				List<MutableCell> cells = new ArrayList<>();
				for (Cell match : matches) {
					// create a result cell for each match

					// if the matching is a Retype/Rename, replace source of
					// the original cell
					if (isDirectMatch(match)) {
						MigrationOptions replaceSource = new MigrationOptionsImpl(true, false,
								transferBase);
						cells.add(getCellMigrator.apply(originalCell.getTransformationIdentifier())
								.updateCell(originalCell, migration, replaceSource, log));
					}
					// if the cell is a Retype/Rename, replace the target of
					// matching cell
					else if (isDirectMatch(originalCell)) {
						MigrationOptions replaceTarget = new MigrationOptionsImpl(false, true,
								transferBase);
						AlignmentMigration cellMigration = new AbstractMigration() {

							@Override
							protected Optional<EntityDefinition> findMatch(
									EntityDefinition entity) {
								Entity target = CellUtil.getFirstEntity(originalCell.getTarget());
								if (target != null) {
									return Optional.ofNullable(target.getDefinition());
								}
								return Optional.empty();
							}
						};
						MutableCell newCell = getCellMigrator
								.apply(match.getTransformationIdentifier())
								.updateCell(match, cellMigration, replaceTarget, log);
						SimpleLog cellLog = SimpleLog.all(log,
								new CellLog(newCell, CELL_LOG_CATEGORY));

						// source of original cell may have
						// filters/conditions/contexts that are not applied by
						// changing the target

						// try to apply source contexts
						Entity originalSource = CellUtil.getFirstEntity(originalCell.getSource());
						applySourceContexts(newCell, originalSource, cellLog);

						cells.add(newCell);
					}
					else {
						// otherwise, use custom logic to try to combine cells

						MutableCell newCell = new DefaultCell(originalCell);
						SimpleLog cellLog = SimpleLog.all(log,
								new CellLog(newCell, CELL_LOG_CATEGORY));
						C context = newContext(originalCell);

						// reset source
						newCell.setSource(ArrayListMultimap.create());

						if (inaccurateMatch) {
							cellLog.warn(MessageFormat
									.format("Inaccurate match of {0} via parent entity", source));
						}

						mergeSource(newCell, sources.keys().iterator().next(), source, match,
								originalCell, cellLog, context, migration, mergeIndex);

						finalize(newCell, migration, context, cellLog);

						cells.add(newCell);
					}
				}

				if (!cells.isEmpty() && !storedMessages.isEmpty()) {
					// add stored messages
					cells.forEach(cell -> {
						CellLog cLog = new CellLog(cell, CELL_LOG_CATEGORY);
						storedMessages.forEach(msg -> cLog.warn(msg));
					});
				}

				return cells;
			}
			else {
				// no match -> remove?
				// rather add original + documentation
				MutableCell newCell = new DefaultCell(originalCell);
				SimpleLog cellLog = SimpleLog.all(log, new CellLog(newCell, CELL_LOG_CATEGORY));

				cellLog.warn(
						"No match for source {0} found, unable to associate to new source schema",
						source);

				return Collections.singleton(newCell);
			}
		}
		else {
			// handle each source

			// collects messages in case all matches are direct matches
			List<String> directMessages = new ArrayList<>();

			// determine if all matches are direct
			boolean allDirect = sources.entries().stream().allMatch(source -> {
				List<Cell> matches = mergeIndex
						.getCellsForTarget(source.getValue().getDefinition());
				if (matches.isEmpty()) {
					directMessages.add(MessageFormat.format(
							"No match was found for source {0}, please check how this can be compensated.",
							source.getValue().getDefinition()));
					// if there is no match, treat it as direct match
					return true;
				}
				else {
					if (matches.size() > 1) {
						directMessages.add(MessageFormat.format(
								"Multiple matches for source {0}, only one was taken into account",
								source.getValue().getDefinition()));
					}

					return isDirectMatch(matches.get(0));
				}
			});

			MutableCell newCell;

			if (allDirect) {
				// if the matching are all Retype/Rename, replace sources of
				// the original cell
				MigrationOptions replaceSource = new MigrationOptionsImpl(true, false,
						transferBase);
				newCell = getCellMigrator.apply(originalCell.getTransformationIdentifier())
						.updateCell(originalCell, migration, replaceSource, log);
				// add messages from match check
				SimpleLog cellLog = SimpleLog.all(log, new CellLog(newCell, CELL_LOG_CATEGORY));
				directMessages.forEach(msg -> cellLog.warn(msg));
			}
			else {
				// handle each source separately
				newCell = new DefaultCell(originalCell);
				SimpleLog cellLog = SimpleLog.all(log, new CellLog(newCell, CELL_LOG_CATEGORY));
				C context = newContext(originalCell);

				// reset source
				newCell.setSource(ArrayListMultimap.create());

				for (Entry<String, ? extends Entity> source : sources.entries()) {
					List<Cell> matches = mergeIndex
							.getCellsForTarget(source.getValue().getDefinition());
					if (!matches.isEmpty()) {
						Cell match = matches.get(0);

						mergeSource(newCell, source.getKey(), source.getValue().getDefinition(),
								match, originalCell, cellLog, context, migration, mergeIndex);

						if (matches.size() > 1) {
							// FIXME how can we deal w/ multiple matches?
							cellLog.warn("Multiple matches for source {0}, only one was handled",
									source.getValue().getDefinition());
						}
					}
					else {
						// no match, just not add source?
						cellLog.warn(
								"No match was found for source {0}, please check how this can be compensated.",
								source.getValue().getDefinition());
					}
				}

				finalize(newCell, migration, context, cellLog);
			}

			return Collections.singleton(newCell);
		}
	}

	private List<Cell> findParentMatch(EntityDefinition entity, MergeIndex mergeIndex) {
		// XXX only allow parent matches for specific cases right now
		if (!(entity.getDefinition() instanceof PropertyDefinition)
				|| !((PropertyDefinition) entity.getDefinition()).getPropertyType()
						.getConstraint(GeometryType.class).isGeometry()) {
			// not a geometry
			return Collections.emptyList();
		}

		while (entity != null) {
			entity = AlignmentUtil.getParent(entity);

			List<Cell> matches = mergeIndex.getCellsForTarget(entity);
			if (!matches.isEmpty()) {
				return matches;
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Apply contexts/conditions from the original source to the source of the
	 * new mapping cell that replaces it.
	 * 
	 * @param newCell the cell to adapt
	 * @param originalSource the original source
	 * @param log the cell log
	 */
	private void applySourceContexts(MutableCell newCell, Entity originalSource, SimpleLog log) {
		if (originalSource != null) {
			EntityDefinition original = originalSource.getDefinition();
			boolean isDefault = AlignmentUtil.isDefaultEntity(original);

			if (!isDefault) {
				ListMultimap<String, ? extends Entity> newSource = newCell.getSource();
				if (newSource == null || newSource.size() == 0) {
					// new cell does not have a source -> drop contexts
					log.warn(
							"Any conditions/contexts on the original source have been dropped because the new mapping does not have a source: "
									+ MergeUtil.getContextInfoString(original));
				}
				else if (newSource.size() == 1) {
					// try to transfer contexts
					Entity singleSource = CellUtil.getFirstEntity(newSource);
					if (singleSource != null) {
						EntityDefinition transferedSource = AbstractMigration
								.translateContexts(original, singleSource.getDefinition(), log);
						ListMultimap<String, Entity> s = ArrayListMultimap.create();
						s.put(newSource.keySet().iterator().next(),
								AlignmentUtil.createEntity(transferedSource));
						newCell.setSource(s);
					}
				}
				else {
					// no idea where to add contexts -> report
					log.warn(
							"Any conditions/contexts on the original source have been dropped because the new mapping has multiple sources and it is not clear where they should be attached: "
									+ MergeUtil.getContextInfoString(original));
				}
			}
		}
	}

	/**
	 * Finalize a created cell based on the context.
	 * 
	 * @param newCell the new merged cell
	 * @param migration the alignment migration
	 * @param context the cell merge context
	 * @param log the cell log
	 */
	protected void finalize(MutableCell newCell, AlignmentMigration migration, C context,
			SimpleLog log) {
		// override me
	}

	/**
	 * Merge a source according to a matching cell.
	 * 
	 * @param cell the target cell to merge the source to (contains only already
	 *            merged sources)
	 * @param sourceName the name of the source
	 * @param source the source definition
	 * @param match the match for the source
	 * @param originalCell the original cell
	 * @param log the migration process log
	 * @param context the context for the creation of the cell (possibly shared
	 *            across multiple calls to
	 *            {@link #mergeSource(MutableCell, String, EntityDefinition, Cell, Cell, SimpleLog, Object, AlignmentMigration, MergeIndex)}
	 *            )
	 * @param migration the alignment migration
	 * @param mergeIndex the merge index
	 */
	protected void mergeSource(MutableCell cell, String sourceName, EntityDefinition source,
			Cell match, Cell originalCell, SimpleLog log, C context, AlignmentMigration migration,
			MergeIndex mergeIndex) {
		// override me
		log.warn("Unsupported combination {0} / {1} for source {2}",
				match.getTransformationIdentifier(), cell.getTransformationIdentifier(), source);
	}

}
