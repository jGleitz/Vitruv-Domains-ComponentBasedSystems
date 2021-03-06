package tools.vitruv.domains.sysml

import org.eclipse.uml2.uml.UMLFactory
import org.eclipse.emf.ecore.EObject
import org.eclipse.uml2.uml.UMLPackage
import org.eclipse.papyrus.sysml14.blocks.BlocksFactory
import org.eclipse.papyrus.sysml14.blocks.Block
import tools.vitruv.framework.tuid.Tuid
import tools.vitruv.framework.tuid.TuidManager
import tools.vitruv.framework.tuid.TuidUpdateListener
import java.util.ArrayList
import java.util.List
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

class SysMlDomainTests {
	static val TEST_CLASS_NAME = "Test";
	var SysMlDomain sysMlDomain;
	
	@BeforeEach
	def void setup() {
		TuidManager.instance.reinitialize
		sysMlDomain = new SysMlDomainProvider().domain;
		sysMlDomain.registerAtTuidManagement;
	}
	
	@Test
	def void testTuidCalculationForUmlElemente() {
		val clazz = UMLFactory.eINSTANCE.createClass();
		clazz.name = TEST_CLASS_NAME;
		testTuid(clazz, "Class", TEST_CLASS_NAME);
		val port = UMLFactory.eINSTANCE.createPort();
		port.name = TEST_CLASS_NAME;
		testTuid(port, "Port", TEST_CLASS_NAME);
	}
	
	@Test
	def void testTuidCalculationForSysMlElements() {
		val block = createBlock();
		testTuid(block, "Class", TEST_CLASS_NAME);
		assertEquals(sysMlDomain.calculateTuid(block), sysMlDomain.calculateTuid(block.base_Class));
	}
	
	@Test
	def void testResponsibilityChecks() {
		val block = createBlock();
		assertTrue(sysMlDomain.isInstanceOfDomainMetamodel(block));
		assertTrue(sysMlDomain.isInstanceOfDomainMetamodel(block.base_Class));
		assertTrue(sysMlDomain.calculateTuid(block) !== null);
		assertTrue(sysMlDomain.calculateTuid(block.base_Class) !== null);
	}
	
	@Test
	def void testTuidUpdate() {
		val List<String> tuids = new ArrayList<String>();
		val dummyTuidUpdateListener = new TuidUpdateListener() {
			override performPreAction(Tuid oldTuid) {
				tuids.add(oldTuid.toString);
			}
			
			override performPostAction(Tuid newTuid) {
				tuids.add(newTuid.toString);
			}
		}
		TuidManager.instance.addTuidUpdateListener(dummyTuidUpdateListener);
		val block = createBlock();
		block.base_Class.name = "old";
		TuidManager.instance.registerObjectUnderModification(block);
		block.base_Class.name = "new";
		TuidManager.instance.updateTuidsOfRegisteredObjects();
		TuidManager.instance.flushRegisteredObjectsUnderModification();
		assertEquals(2, tuids.size);
		testTuid(tuids.get(0), "Class", "old");
		testTuid(tuids.get(1), "Class", "new");
	}
	
	private def Block createBlock() {
		val clazz = UMLFactory.eINSTANCE.createClass();
		clazz.name = TEST_CLASS_NAME;
		val block = BlocksFactory.eINSTANCE.createBlock();
		block.base_Class = clazz;
		return block;
	}
	
	private def void testTuid(EObject object, String expectedTypeName, String expectedName) {
		assertTuid(object, UMLPackage.eNS_URI, "<root>-_-" + expectedTypeName + "-_-" + UMLPackage.Literals.NAMED_ELEMENT__NAME.name + "=" + expectedName);
	}
	
	private def void testTuid(String tuid, String expectedTypeName, String expectedName) {
		assertTuid(tuid, UMLPackage.eNS_URI, "<root>-_-" + expectedTypeName + "-_-" + UMLPackage.Literals.NAMED_ELEMENT__NAME.name + "=" + expectedName);
	}
	
	private def void assertTuid(EObject object, String expectedNamespaceUri, String expectedIdentifier) {
		val tuid = sysMlDomain.calculateTuid(object).toString;
		assertTuid(tuid, expectedNamespaceUri, expectedIdentifier);
	}
	
	private def void assertTuid(String tuid, String expectedNamespaceUri, String expectedIdentifier) {
		val tuidFragments = tuid.split("#");
		assertEquals(3, tuidFragments.length);
		assertEquals(expectedNamespaceUri, tuidFragments.get(0));
		assertNotNull(tuidFragments.get(1));
		assertEquals(expectedIdentifier, tuidFragments.get(2));
	}
	
}