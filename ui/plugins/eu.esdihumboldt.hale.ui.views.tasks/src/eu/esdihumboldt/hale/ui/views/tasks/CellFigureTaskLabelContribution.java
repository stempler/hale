/*
 * Copyright (c) 2018 wetransform GmbH
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

package eu.esdihumboldt.hale.ui.views.tasks;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fhg.igd.slf4jplus.ALogger;
import de.fhg.igd.slf4jplus.ALoggerFactory;
import eu.esdihumboldt.hale.common.align.model.Cell;
import eu.esdihumboldt.hale.common.tasks.ResolvedTask;
import eu.esdihumboldt.hale.common.tasks.TaskService;
import eu.esdihumboldt.hale.common.tasks.TaskServiceListener;
import eu.esdihumboldt.hale.ui.HaleUI;
import eu.esdihumboldt.hale.ui.common.CommonSharedImages;
import eu.esdihumboldt.hale.ui.common.graph.figures.CellFigure;
import eu.esdihumboldt.hale.ui.common.graph.figures.CellFigureContribution;

/**
 * Contribution to {@link CellFigure} labels for {@link Cell}s that have at
 * least one open task
 * 
 * @author Florian Esser
 */
public class CellFigureTaskLabelContribution implements CellFigureContribution {

	private static final ALogger log = ALoggerFactory
			.getLogger(CellFigureTaskLabelContribution.class);

	private TaskServiceListener taskServiceListener;

	@Override
	public void contribute(CellFigure figure, Cell cell) {
		TaskService taskService = HaleUI.getServiceProvider().getService(TaskService.class);
		Collection<ResolvedTask<Cell>> tasks = taskService.getTasks(cell).stream()
				.map(t -> taskService.resolveTask(t)).collect(Collectors.toList());

		tasks = tasks.stream().filter(t -> t.isOpen()).collect(Collectors.toList());
		if (tasks.isEmpty()) {
			return;
		}

		Label tasksLabel = new Label();
		Image tasksImage = null;
		tasksImage = CommonSharedImages.getImageRegistry().get(CommonSharedImages.IMG_TASKS);
		tasksLabel.setIcon(tasksImage);
		tasksLabel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent me) {
				try {
					IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(TaskTreeView.ID);
					// XXX better would be if we could trigger the selection in
					// the alignment/mapping view
					if (part instanceof TaskTreeView) {
						((TaskTreeView) part).update(new StructuredSelection(cell));
					}
					me.consume();
				} catch (PartInitException e) {
					log.error("Error creating task view", e);
				}
			}

			@Override
			public void mousePressed(MouseEvent me) {
				// ignore
			}

			@Override
			public void mouseDoubleClicked(MouseEvent me) {
				// ignore
			}
		});
		if (tasksImage != null) {
			Label priorityTip = new Label(MessageFormat.format(
					"There {0} {1} open tasks for this cell. Please refer to the Tasks view for details.",
					(tasks.size() == 1) ? "is" : "are", tasks.size()));
			tasksLabel.setToolTip(priorityTip);
		}
		GridData tasksLabelGD = new GridData(GridData.CENTER, GridData.FILL, false, true);
		figure.add(tasksLabel, tasksLabelGD);

	}

	/**
	 * @see eu.esdihumboldt.hale.ui.common.graph.figures.CellFigureContribution#getLabelColumnCount()
	 */
	@Override
	public int getLabelColumnCount() {
		return 1;
	}

}
