package tools.vitruv.domains.java.ui.monitorededitor.util

import tools.vitruv.domains.java.ui.monitorededitor.astchangelistener.classification.ConcreteChangeClassifier
import tools.vitruv.framework.util.bridges.EclipseBridge
import java.util.List
import tools.vitruv.framework.util.VitruviusConstants
import edu.kit.ipd.sdq.activextendannotations.Utility
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.ChangeEventExtendedVisitor

@Utility
class ExtensionPointsUtil {
	static def getRegisteredAstPostChangeClassifiers() {
		getRegisteredExtensions("tools.vitruv.domains.java.ui.monitorededitor.astpostchange", ConcreteChangeClassifier)	
	}
	
	static def getRegisteredAstPostReconcileClassifiers() {
		getRegisteredExtensions("tools.vitruv.domains.java.ui.monitorededitor.astpostreconcile", ConcreteChangeClassifier)	
	}
	
	static def getRegisteredChangeEventExtendedVisitors() {
		getRegisteredExtensions("tools.vitruv.domains.java.ui.monitorededitor.changeeventextendedvisitors", ChangeEventExtendedVisitor)	
	}
	
	private static def <T> List<T> getRegisteredExtensions(String extensionPointName, Class<T> expectedType) {
		return EclipseBridge.getRegisteredExtensions(extensionPointName, VitruviusConstants.getExtensionPropertyName(),
				expectedType);
	}
}