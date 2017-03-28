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

package eu.esdihumboldt.hale.common.filter;

import static org.junit.Assert.*

import org.junit.Ignore
import org.junit.Test

import eu.esdihumboldt.hale.common.instance.model.Filter
import eu.esdihumboldt.hale.common.instance.model.Instance
import groovy.transform.CompileStatic

/**
 * Tests for CQL filter.
 * 
 * Should cover all basic filter operators.
 * See <a href="http://udig.github.io/docs/user/concepts/Constraint%20Query%20Language.html">CQL documentation</a>.
 * 
 * @author Simon Templer
 */
@CompileStatic
class CQLFilterTest extends AbstractFilterTest {

	Filter filter(String expr) {
		new FilterGeoCqlImpl(expr)
	}

	// EQUALITY (=)

	@Test
	void testEqualitySimple() {
		assertTrue(filter("name = 'Max Mustermann'").match(max))
		assertFalse(filter("name = 'max mustermann'").match(max))
	}

	@Test
	void testEqualitySimpleNoSchema() {
		assertTrue(filter("name = 'Max Mustermann'").match(maxNoSchema))
		assertFalse(filter("name = 'max mustermann'").match(maxNoSchema))
	}

	@Test
	void testEqualityList() {
		// Behavior: only one occurrence in the list of values needs to match
		assertTrue(filter("address.street = 'Musterstrasse'").match(max))
		assertTrue(filter("address.street = 'Taubengasse'").match(max))
		assertTrue(filter("address.city = 'Musterstadt'").match(max))
	}

	@Test
	void testEqualityListNoSchema() {
		// Behavior: only one occurrence in the list of values needs to match
		assertTrue(filter("address.street = 'Musterstrasse'").match(maxNoSchema))
		assertTrue(filter("address.street = 'Taubengasse'").match(maxNoSchema))
		assertTrue(filter("address.city = 'Musterstadt'").match(maxNoSchema))
	}

	// NULL

	@Test
	void testNullSchema() {
		assertTrue(filter("legalStatus IS NULL").match(max))
		assertFalse(filter("legalStatus IS NOT NULL").match(max))
	}

	@Test
	void testNullNoSchema() {
		assertTrue(filter("legalStatus IS NULL").match(maxNoSchema))
		assertFalse(filter("legalStatus IS NOT NULL").match(maxNoSchema))
	}

	@Test
	void testNullList() {
		//XXX The "IS NULL" filter only succeeds if there is a single null value or no value!
		// in this it differs from other filter operators
		assertFalse(filter("nulls IS NULL").match(max))
		assertTrue(filter("nulls IS NOT NULL").match(max))
	}

	@Test
	void testNullNotExistsSchema() {
		assertTrue(filter("doesNotExist IS NULL").match(max))
		assertFalse(filter("doesNotExist IS NOT NULL").match(max))
	}

	@Test
	void testNullNotExistsNoSchema() {
		assertTrue(filter("doesNotExist IS NULL").match(maxNoSchema))
		assertFalse(filter("doesNotExist IS NOT NULL").match(maxNoSchema))
	}

	@Test
	void testInstanceNullSchema() {
		assertTrue(filter("address IS NOT NULL").match(max))
		assertFalse(filter("address IS NULL").match(max))
	}

	@Test
	void testInstanceValueNullSchema() {
		assertTrue(filter("relative IS NOT NULL").match(max))
		assertFalse(filter("relative IS NULL").match(max))
	}

	@Test
	void testInstanceNoValueNullSchema() {
		assertTrue(filter("friend IS NULL").match(max))
		assertFalse(filter("friend IS NOT NULL").match(max))
	}

	@Test
	void testInstanceNoValueNullNoSchema() {
		// Behavior: This only checks the instance value
		assertTrue(filter("friend IS NULL").match(maxNoSchema))
		assertFalse(filter("friend IS NOT NULL").match(maxNoSchema))
	}

	// EXISTS / DOES-NOT-EXIST

	@Ignore('PropertyExistsFunction does not support instances')
	@Test
	void testExistsSchema() {
		assertTrue(filter("name EXISTS").match(max))
		assertFalse(filter("name DOES-NOT-EXIST").match(max))
	}

	@Ignore('PropertyExistsFunction does not support instances')
	@Test
	void testExistsNoSchema() {
		assertTrue(filter("name EXISTS").match(maxNoSchema))
		assertFalse(filter("name DOES-NOT-EXIST").match(maxNoSchema))
	}

	@Ignore('PropertyExistsFunction does not support instances')
	@Test
	void testInstanceExistsSchema() {
		assertTrue(filter("address EXISTS").match(max))
	}

	@Ignore('PropertyExistsFunction does not support instances')
	@Test
	void testInstanceValueExistsSchema() {
		assertTrue(filter("relative EXISTS").match(max))
	}

	@Ignore('PropertyExistsFunction does not support instances')
	@Test
	void testInstanceNoValueExistsSchema() {
		assertTrue(filter("friend EXISTS").match(max))
	}

	@Test
	void testDoesNotExistSchema() {
		assertTrue(filter("doesNotExist DOES-NOT-EXIST").match(max))
		assertFalse(filter("doesNotExist EXISTS").match(max))
	}

	// LIKE

	@Test
	void testLikeSchema() {
		assertTrue(filter("name LIKE 'Max %'").match(max))
		assertFalse(filter("name LIKE 'Martha %'").match(max))
		assertTrue(filter("name NOT LIKE 'Martha %'").match(max))
		assertFalse(filter("name NOT LIKE 'Max %'").match(max))
	}

	@Test
	void testLikeNoSchema() {
		assertTrue(filter("name LIKE 'Max %'").match(maxNoSchema))
		assertFalse(filter("name LIKE 'Martha %'").match(maxNoSchema))
	}

	// Number comparisons

	@Test
	void testNumber() {
		assertTrue(filter("age = 31").match(max))
		assertTrue(filter("age > 30").match(max))
		assertTrue(filter("age > 0").match(max))
		assertFalse(filter("age < 30").match(max))
		assertTrue(filter("age >= 31").match(max))
		assertFalse(filter("age >= 32").match(max))
		assertTrue(filter("age <= 31").match(max))
		assertFalse(filter("age <= 30").match(max))
	}

	@Test
	void testNumberString() {
		// Behavior: Number also matches equal string literal
		assertTrue(filter("age = '31'").match(max))
	}

	@Test
	void testNumberList() {
		// Behavior: only one occurrence in the list of values needs to match
		assertTrue(filter("address.number = 12").match(max))
		assertTrue(filter("address.number > 12").match(max))
		assertTrue(filter("address.number > 0").match(max))
		assertFalse(filter("address.number < 10").match(max))
		assertTrue(filter("address.number < 13").match(max))

		// more complex tests
		assertTrue(filter("address.number > 10 AND address.number <= 12").match(max))
		assertFalse(filter("address.number > 20 AND address.number <= 30").match(max))
	}

	// BETWEEN

	@Test
	void testNumberBetween() {
		assertTrue(filter("age BETWEEN 20 AND 40").match(max))
		assertFalse(filter("age BETWEEN 10 AND 20").match(max))
	}

	@Test
	void testStringBetween() {
		// Behavior: Strings are compared with standard Java String comparison
		assertTrue(filter("name BETWEEN 'A' AND 'Z'").match(max))
		assertTrue(filter("name BETWEEN 'G' AND 'O'").match(max))
		assertFalse(filter("name BETWEEN 'a' AND 'z'").match(max))
	}

	@Test
	void testNumberBetweenList() {
		assertTrue(filter("address.number BETWEEN 10 AND 20").match(max))
		assertTrue(filter("address.number BETWEEN 10 AND 12").match(max))
		assertFalse(filter("address.number BETWEEN 1 AND 10").match(max))
	}

}