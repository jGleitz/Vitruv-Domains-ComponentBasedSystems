package tools.vitruv.domains.java.ui.monitorededitor.changeclassification.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.emftext.language.java.annotations.AnnotationInstance;
import org.emftext.language.java.classifiers.Class;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.commons.NamedElement;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.imports.Import;
import org.emftext.language.java.members.Field;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.modifiers.AnnotableAndModifiable;
import org.emftext.language.java.modifiers.Modifier;
import org.emftext.language.java.parameters.Parameter;
import org.emftext.language.java.parameters.Parametrizable;
import org.emftext.language.java.types.PrimitiveType;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypeReference;

import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.ChangeEventExtendedVisitor;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.ChangeEventVisitor;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.AddAnnotationEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.AddFieldEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.AddImportEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.AddJavaDocEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.AddMethodEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.AddSuperClassEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.AddSuperInterfaceEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.ChangeClassModifiersEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.ChangeClassifyingEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.ChangeClassifyingEventExtension;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.ChangeFieldModifiersEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.ChangeFieldTypeEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.ChangeInterfaceModifiersEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.ChangeJavaDocEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.ChangeMethodModifiersEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.ChangeMethodParameterEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.ChangeMethodReturnTypeEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.ChangePackageDeclarationEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.CreateClassEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.CreateInterfaceEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.CreatePackageEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.DeleteClassEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.DeleteInterfaceEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.DeletePackageEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.MoveMethodEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RemoveAnnotationEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RemoveFieldEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RemoveImportEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RemoveJavaDocEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RemoveMethodEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RemoveSuperClassEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RemoveSuperInterfaceEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RenameClassEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RenameFieldEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RenameInterfaceEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RenameMethodEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RenamePackageDeclarationEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RenamePackageEvent;
import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events.RenameParameterEvent;
import tools.vitruv.domains.java.ui.monitorededitor.jamopputil.AST2Jamopp;
import tools.vitruv.domains.java.ui.monitorededitor.jamopputil.CompilationUnitAdapter;
import tools.vitruv.domains.java.ui.monitorededitor.jamopputil.JamoppChangeBuildHelper;
import tools.vitruv.framework.change.description.CompositeContainerChange;
import tools.vitruv.framework.change.description.ConcreteChange;
import tools.vitruv.framework.change.description.VitruviusChange;
import tools.vitruv.framework.change.description.VitruviusChangeFactory;
import tools.vitruv.framework.util.datatypes.VURI;
import static tools.vitruv.domains.java.ui.monitorededitor.util.ExtensionPointsUtil.getRegisteredChangeEventExtendedVisitors;

/**
 * The {@link ChangeClassifyingEventToVitruviusChangeConverterImpl} implements a
 * {@link ChangeEventVisitor} for {@link ChangeClassifyingEvent}s. It uses the
 * AST information in the change events to build {@link VitruviusChange}s with
 * the {@link JamoppChangeBuildHelper}. It implements the
 * {@link ChangeClassifyingEventToVitruviusChangeConverter} such that a given
 * {@link ChangeClassifyingEvent} can be converted into a
 * {@link VitruviusChange}.
 */
public class ChangeClassifyingEventToVitruviusChangeConverterImpl
		implements ChangeEventVisitor<Optional<VitruviusChange>>, ChangeClassifyingEventToVitruviusChangeConverter {
	private static final Logger logger = Logger.getLogger(ChangeClassifyingEventToVitruviusChangeConverterImpl.class);

	private final Map<java.lang.Class<? extends ChangeClassifyingEventExtension>, ChangeEventExtendedVisitor> dispatcher;
	private final ChangeResponderUtility util;

	public ChangeClassifyingEventToVitruviusChangeConverterImpl() {
		this.util = new ChangeResponderUtility();
		this.dispatcher = new HashMap<java.lang.Class<? extends ChangeClassifyingEventExtension>, ChangeEventExtendedVisitor>();
		this.fillDispatcherMap();
	}

	private void fillDispatcherMap() {
		for (final ChangeEventExtendedVisitor visitor : getRegisteredChangeEventExtendedVisitors()) {
			for (final java.lang.Class<? extends ChangeClassifyingEventExtension> clazz : visitor.getTreatedClasses()) {
				this.dispatcher.put(clazz, visitor);
			}
		}
	}

	@Override
	public Optional<VitruviusChange> convert(ChangeClassifyingEvent event) {
		return event.accept(this);
	}

	@Override
	public Optional<VitruviusChange> visit(final ChangeClassifyingEventExtension changeClassifyingEvent) {
		return this.dispatcher.get(changeClassifyingEvent.getClass()).visit(changeClassifyingEvent);
	}

	@Override
	public Optional<VitruviusChange> visit(final AddMethodEvent addMethodEvent) {
		final MethodDeclaration newMethodDeclaration = addMethodEvent.method;
		final CompilationUnitAdapter originalCU = this.util.getUnsavedCompilationUnitAdapter(newMethodDeclaration);
		final Parametrizable newMethodOrConstructor = originalCU
				.getMethodOrConstructorForMethodDeclaration(newMethodDeclaration);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(addMethodEvent.typeBeforeAdd);
		final ConcreteClassifier classifierBeforeAdd = changedCU
				.getConcreteClassifierForTypeDeclaration(addMethodEvent.typeBeforeAdd);
		final EChange eChange = JamoppChangeBuildHelper.createAddMethodChange(newMethodOrConstructor,
				classifierBeforeAdd);
		return this.util.createVitruviusChange(eChange, addMethodEvent.method);
	}

	@Override
	public Optional<VitruviusChange> visit(final CreateInterfaceEvent createInterfaceEvent) {
		final TypeDeclaration type = createInterfaceEvent.type;
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(createInterfaceEvent.compilationUnitBeforeCreate);
		final CompilationUnitAdapter changedCU = this.util.getUnsavedCompilationUnitAdapter(type);
		final Interface newInterface = (Interface) changedCU.getConcreteClassifierForTypeDeclaration(type);
		final EChange eChange = JamoppChangeBuildHelper.createCreateInterfaceChange(newInterface,
				null == originalCU ? null : originalCU.getCompilationUnit());
		return this.util.createVitruviusChange(eChange, createInterfaceEvent.type);
	}

	@Override
	public Optional<VitruviusChange> visit(final CreateClassEvent createClassEvent) {
		final TypeDeclaration type = createClassEvent.type;
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(createClassEvent.compilationUnitBeforeCreate);
		final CompilationUnitAdapter changedCU = this.util.getUnsavedCompilationUnitAdapter(type);
		final Class newClass = (Class) changedCU.getConcreteClassifierForTypeDeclaration(type);
		final CompilationUnit beforeChange = null == originalCU ? null : originalCU.getCompilationUnit();
		final EChange eChange = JamoppChangeBuildHelper.createAddClassChange(newClass, beforeChange);
		return this.util.createVitruviusChange(eChange, createClassEvent.type);
	}

	@Override
	public Optional<VitruviusChange> visit(final ChangeMethodReturnTypeEvent changeMethodReturnTypeEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(changeMethodReturnTypeEvent.original);
		final Parametrizable original = originalCU
				.getMethodOrConstructorForMethodDeclaration(changeMethodReturnTypeEvent.original);
		final CompilationUnitAdapter cu = this.util
				.getUnsavedCompilationUnitAdapter(changeMethodReturnTypeEvent.renamed);
		final Parametrizable changed = cu
				.getMethodOrConstructorForMethodDeclaration(changeMethodReturnTypeEvent.renamed);
		if (changed instanceof Method && original instanceof Method) {
			final EChange eChange = JamoppChangeBuildHelper.createChangeMethodReturnTypeChange((Method) original,
					(Method) changed);
			return this.util.createVitruviusChange(eChange, changeMethodReturnTypeEvent.original);
		} else {
			logger.info(
					"Change method return type could not be reported. Either original or changed is not instanceof method: orginal: "
							+ " changed: " + changed);
			return Optional.empty();
		}
	}

	@Override
	public Optional<VitruviusChange> visit(final RemoveMethodEvent removeMethodEvent) {
		final CompilationUnitAdapter originalCU = this.util.getUnsavedCompilationUnitAdapter(removeMethodEvent.method);
		final Parametrizable removedMethod = originalCU
				.getMethodOrConstructorForMethodDeclaration(removeMethodEvent.method);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(removeMethodEvent.typeAfterRemove);
		final ConcreteClassifier classifierAfterRemove = changedCU
				.getConcreteClassifierForTypeDeclaration(removeMethodEvent.typeAfterRemove);
		final EChange eChange = JamoppChangeBuildHelper.createRemoveMethodChange(removedMethod, classifierAfterRemove);
		return this.util.createVitruviusChange(eChange, removeMethodEvent.method);
	}

	@Override
	public Optional<VitruviusChange> visit(final DeleteClassEvent deleteClassEvent) {
		final TypeDeclaration type = deleteClassEvent.type;
		final CompilationUnitAdapter originalCU = this.util.getUnsavedCompilationUnitAdapter(type);
		final Class deletedClass = (Class) originalCU.getConcreteClassifierForTypeDeclaration(type);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(deleteClassEvent.compilationUnitAfterDelete);
		final EChange eChange = JamoppChangeBuildHelper.createRemovedClassChange(deletedClass,
				changedCU.getCompilationUnit());
		return this.util.createVitruviusChange(eChange, deleteClassEvent.type);
	}

	@Override
	public Optional<VitruviusChange> visit(final DeleteInterfaceEvent deleteInterfaceEvent) {
		final TypeDeclaration type = deleteInterfaceEvent.type;
		final CompilationUnitAdapter oldCU = this.util.getUnsavedCompilationUnitAdapter(type);
		final Interface deletedInterface = (Interface) oldCU.getConcreteClassifierForTypeDeclaration(type);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(deleteInterfaceEvent.compilationUnitAfterDelete);
		final EChange eChange = JamoppChangeBuildHelper.createRemovedInterfaceChange(deletedInterface,
				changedCU.getCompilationUnit());
		return this.util.createVitruviusChange(eChange, deleteInterfaceEvent.type);
	}

	@Override
	public Optional<VitruviusChange> visit(final RenameMethodEvent renameMethodEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(renameMethodEvent.original);
		final Parametrizable original = originalCU
				.getMethodOrConstructorForMethodDeclaration(renameMethodEvent.original);
		final URI uri = this.util.getFirstExistingURI(renameMethodEvent.renamed, renameMethodEvent.original);
		final CompilationUnitAdapter changedCU = this.util.getUnsavedCompilationUnitAdapter(renameMethodEvent.renamed,
				uri);
		final Parametrizable changed = changedCU.getMethodOrConstructorForMethodDeclaration(renameMethodEvent.renamed);
		if (changed instanceof Member && original instanceof Member) {
			final EChange eChange = JamoppChangeBuildHelper.createRenameMethodChange((Member) original,
					(Member) changed);
			return this.util.createVitruviusChange(eChange, renameMethodEvent.original);
		} else {
			logger.info(
					"Could not execute rename method event, cause original or changed is not instance of Member. Original: "
							+ original + " Changed: " + changed);
			return Optional.empty();
		}
	}

	@Override
	public Optional<VitruviusChange> visit(final RenameFieldEvent renameFieldEvent) {
		final CompilationUnitAdapter originalCU = this.util.getUnsavedCompilationUnitAdapter(renameFieldEvent.original);
		final Field original = originalCU.getFieldForVariableDeclarationFragment(renameFieldEvent.originalFragment);
		final URI uri = this.util.getFirstExistingURI(renameFieldEvent.changed, renameFieldEvent.original);
		final CompilationUnitAdapter changedCU = this.util.getUnsavedCompilationUnitAdapter(renameFieldEvent.changed,
				uri);
		final Field renamed = changedCU.getFieldForVariableDeclarationFragment(renameFieldEvent.changedFragment);
		final EChange eChange = JamoppChangeBuildHelper.createRenameFieldChange(original, renamed);
		return this.util.createVitruviusChange(eChange, renameFieldEvent.original);
	}

	@Override
	public Optional<VitruviusChange> visit(final RenameInterfaceEvent renameInterfaceEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(renameInterfaceEvent.original);
		final Interface originalInterface = (Interface) originalCU
				.getConcreteClassifierForTypeDeclaration(renameInterfaceEvent.original);
		final URI uri = this.util.getFirstExistingURI(renameInterfaceEvent.renamed, renameInterfaceEvent.original);
		final CompilationUnitAdapter cuRenamed = this.util
				.getUnsavedCompilationUnitAdapter(renameInterfaceEvent.renamed, uri);
		final Interface renamedInterface = (Interface) cuRenamed
				.getConcreteClassifierForTypeDeclaration(renameInterfaceEvent.renamed);

		final EChange eChange = JamoppChangeBuildHelper.createRenameInterfaceChange(originalInterface,
				renamedInterface);
		return this.util.createVitruviusChange(eChange, renameInterfaceEvent.original);
	}

	@Override
	public Optional<VitruviusChange> visit(final RenameClassEvent renameClassEvent) {
		final CompilationUnitAdapter originalCU = this.util.getUnsavedCompilationUnitAdapter(renameClassEvent.original);
		final Class originalClass = (Class) originalCU
				.getConcreteClassifierForTypeDeclaration(renameClassEvent.original);
		final URI uri = this.util.getFirstExistingURI(renameClassEvent.renamed, renameClassEvent.original);
		final CompilationUnitAdapter changedCU = this.util.getUnsavedCompilationUnitAdapter(renameClassEvent.renamed,
				uri);
		final Class renamedClass = (Class) changedCU.getConcreteClassifierForTypeDeclaration(renameClassEvent.renamed);
		final EChange eChange = JamoppChangeBuildHelper.createRenameClassChange(originalClass, renamedClass);
		return this.util.createVitruviusChange(eChange, renameClassEvent.original);
	}

	@Override
	public Optional<VitruviusChange> visit(final AddImportEvent addImportEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(addImportEvent.importDeclaration);
		final Import imp = originalCU.getImportForImportDeclaration(addImportEvent.importDeclaration);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(addImportEvent.compilationUnitBeforeAdd);
		final EChange eChange = JamoppChangeBuildHelper.createAddImportChange(imp, changedCU.getCompilationUnit());
		return this.util.createVitruviusChange(eChange, addImportEvent.importDeclaration);
	}

	@Override
	public Optional<VitruviusChange> visit(final RemoveImportEvent removeImportEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(removeImportEvent.importDeclaration);
		final Import imp = originalCU.getImportForImportDeclaration(removeImportEvent.importDeclaration);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(removeImportEvent.compilationUnitAfterRemove);
		final EChange eChange = JamoppChangeBuildHelper.createRemoveImportChange(imp, changedCU.getCompilationUnit());
		return this.util.createVitruviusChange(eChange, removeImportEvent.importDeclaration);
	}

	@Override
	public Optional<VitruviusChange> visit(final MoveMethodEvent moveMethodEvent) {
		final CompilationUnitAdapter originalCU = this.util.getUnsavedCompilationUnitAdapter(moveMethodEvent.original);
		final CompilationUnitAdapter changedCU = this.util.getUnsavedCompilationUnitAdapter(moveMethodEvent.moved);
		final ConcreteClassifier classifierMovedFromAfterRemove = originalCU
				.getConcreteClassifierForTypeDeclaration(moveMethodEvent.typeMovedFromAfterRemove);
		final ConcreteClassifier classifierMovedToBeforeAdd = originalCU
				.getConcreteClassifierForTypeDeclaration(moveMethodEvent.typeMovedToBeforeAdd);
		final Parametrizable removedParametrizable = originalCU
				.getMethodOrConstructorForMethodDeclaration(moveMethodEvent.original);
		final Parametrizable addedParametrizable = changedCU
				.getMethodOrConstructorForMethodDeclaration(moveMethodEvent.moved);
		if (removedParametrizable instanceof Method && addedParametrizable instanceof Method) {
			final Method addedMethod = (Method) addedParametrizable;
			final Method removedMethod = (Method) removedParametrizable;

			final EChange[] eChanges = JamoppChangeBuildHelper.createMoveMethodChange(removedMethod,
					classifierMovedFromAfterRemove, addedMethod, classifierMovedToBeforeAdd);
			final CompositeContainerChange moveMethodChange = VitruviusChangeFactory.getInstance()
					.createCompositeContainerChange();
			// [0] is remove, [1] is add
			final ConcreteChange removeMethodChange = this.util.wrapToVitruviusModelChange(eChanges[0],
					moveMethodEvent.original);
			final ConcreteChange addMethodChange = this.util.wrapToVitruviusModelChange(eChanges[1],
					moveMethodEvent.moved);
			moveMethodChange.addChange(removeMethodChange);
			moveMethodChange.addChange(addMethodChange);
			return Optional.of(moveMethodChange);
		} else {
			logger.info("could not report move method because either added or removed method is not a method. Added: "
					+ addedParametrizable + " Removed: " + removedParametrizable);
			return Optional.empty();
		}
	}

	@Override
	public Optional<VitruviusChange> visit(final AddSuperInterfaceEvent addSuperInterfaceEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(addSuperInterfaceEvent.baseType);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(addSuperInterfaceEvent.superType);
		if (!(addSuperInterfaceEvent.superType instanceof SimpleType)) {
			logger.warn("visit AddSuperInterfaceEvent failed: super type is not an instance of SimpleType: "
					+ addSuperInterfaceEvent.superType);
			return Optional.empty();
		}
		final TypeReference implementsTypeRef = changedCU
				.getImplementsForSuperType((SimpleType) addSuperInterfaceEvent.superType);
		final ConcreteClassifier affectedClassifier = originalCU
				.getConcreteClassifierForTypeDeclaration(addSuperInterfaceEvent.baseType);
		final EChange eChange = JamoppChangeBuildHelper.createAddSuperInterfaceChange(affectedClassifier,
				implementsTypeRef);
		return this.util.createVitruviusChange(eChange, addSuperInterfaceEvent.baseType);
	}

	@Override
	public Optional<VitruviusChange> visit(final RemoveSuperInterfaceEvent removeSuperInterfaceEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(removeSuperInterfaceEvent.baseType);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(removeSuperInterfaceEvent.superType);
		if (!(removeSuperInterfaceEvent.superType instanceof SimpleType)) {
			logger.warn("visit AddSuperInterfaceEvent failed: super type is not an instance of SimpleType: "
					+ removeSuperInterfaceEvent.superType);
			return Optional.empty();
		}
		final TypeReference implementsTypeRef = originalCU
				.getImplementsForSuperType((SimpleType) removeSuperInterfaceEvent.superType);
		final ConcreteClassifier affectedClassifier = changedCU
				.getConcreteClassifierForTypeDeclaration(removeSuperInterfaceEvent.baseType);
		final EChange eChange = JamoppChangeBuildHelper.createRemoveSuperInterfaceChange(affectedClassifier,
				implementsTypeRef);
		return this.util.createVitruviusChange(eChange, removeSuperInterfaceEvent.baseType);
	}

	@Override
	public Optional<VitruviusChange> visit(final AddSuperClassEvent addSuperClassEvent) {
		logger.warn("AddSuperClassEvent not supported yet");
		return Optional.empty();
	}

	@Override
	public Optional<VitruviusChange> visit(final RemoveSuperClassEvent removeSuperClassEvent) {
		logger.warn("RemoveSuperClassEvent not supported yet");
		return Optional.empty();
	}

	@Override
	public Optional<VitruviusChange> visit(final ChangeMethodParameterEvent changeMethodParameterEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(changeMethodParameterEvent.original);
		final Parametrizable original = originalCU
				.getMethodOrConstructorForMethodDeclaration(changeMethodParameterEvent.original);
		final CompilationUnitAdapter cu = this.util
				.getUnsavedCompilationUnitAdapter(changeMethodParameterEvent.renamed);
		final Parametrizable changed = cu
				.getMethodOrConstructorForMethodDeclaration(changeMethodParameterEvent.renamed);
		return this.handleParameterChanges(changed, original, original.getParameters(), changed.getParameters(),
				changeMethodParameterEvent.original);
	}

	private Optional<VitruviusChange> handleParameterChanges(final Parametrizable methodAfterRemove,
			final Parametrizable methodBeforeAdd, final List<Parameter> oldParameters,
			final List<Parameter> newParameters, final ASTNode oldNode) {
		final CompositeContainerChange compositeChange = VitruviusChangeFactory.getInstance()
				.createCompositeContainerChange();
		/*
		 * for (final Parameter oldParameter : oldParameters) { final EChange eChange =
		 * JaMoPPChangeBuildHelper.createRemoveParameterChange(oldParameter,
		 * methodAfterRemove);
		 * compositeChange.addChange(this.util.wrapToEMFModelChange(eChange, oldNode));
		 * } for (final Parameter newParameter : newParameters) { final EChange eChange
		 * = JaMoPPChangeBuildHelper.createAddParameterChange(newParameter,
		 * methodBeforeAdd);
		 * compositeChange.addChange(this.util.wrapToEMFModelChange(eChange, oldNode));
		 * }
		 */

		// diff the parameter list to figure out which parameters are added respectievly
		// removed
		for (final Parameter oldParameter : oldParameters) {
			if (!this.containsParameter(oldParameter, newParameters)) {
				// old Parameter is no longer contained in newParameters list
				final EChange eChange = JamoppChangeBuildHelper.createRemoveParameterChange(oldParameter,
						methodAfterRemove);
				compositeChange.addChange(this.util.wrapToVitruviusModelChange(eChange, oldNode));
			}
		}
		for (final Parameter newParameter : newParameters) {
			if (!this.containsParameter(newParameter, oldParameters)) {
				// new Parameter is not contained in oldParameters list --> new Parameter has
				// been created
				final EChange eChange = JamoppChangeBuildHelper.createAddParameterChange(newParameter, methodBeforeAdd);
				compositeChange.addChange(this.util.wrapToVitruviusModelChange(eChange, oldNode));
			}
		}
		return Optional.of(compositeChange);
	}

	boolean containsParameter(final Parameter parameter, final List<Parameter> parameterList) {
		for (final Parameter parameterInList : parameterList) {
			// we consider parameters equal if they name is identical, the return type is
			// identical, and they arrayDimension is equal
			if (parameterInList.getName().equals(parameter.getName())
					&& targetInTypeReferenceEquals(parameter.getTypeReference(), parameterInList.getTypeReference())
					&& parameterInList.getArrayDimension() == parameter.getArrayDimension()) {
				return true;
			}
		}
		return false;
	}

	private boolean targetInTypeReferenceEquals(final TypeReference typeRef1, final TypeReference typeRef2) {
		if (typeRef1.getTarget() == null && typeRef2.getTarget() == null) {
			return true;
		}
		if (typeRef1.getTarget() == null) {
			return false;
		}
		if (typeRef2.getTarget() == null) {
			return false;
		}
		if (!typeEquals(typeRef1.getTarget(), typeRef2.getTarget())) {
			return false;
		}
		return true;
	}

	private static boolean typeEquals(final Type type1, final Type type2) {
		if (type1 == type2) {
			return true;
		}

		final boolean sameType = type1.getClass().equals(type2.getClass());
		if (!sameType) {
			// both types have to be from the same type e.g. ConcreteClassifier
			return false;
		}
		if (type1 instanceof PrimitiveType && type2 instanceof PrimitiveType) {
			// both have the same type and they are primitive types-->same type
			return true;
		}
		if (type1 instanceof NamedElement && type2 instanceof NamedElement) {
			final NamedElement ne1 = (NamedElement) type1;
			final NamedElement ne2 = (NamedElement) type2;
			return ne1.getName().equals(ne2.getName());
		}
		return false;
	}

	@Override
	public Optional<VitruviusChange> visit(final ChangeMethodModifiersEvent changeMethodModifierEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(changeMethodModifierEvent.original);
		final Parametrizable originalMethod = originalCU
				.getMethodOrConstructorForMethodDeclaration(changeMethodModifierEvent.original);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(changeMethodModifierEvent.renamed);
		final Parametrizable changedMethod = changedCU
				.getMethodOrConstructorForMethodDeclaration(changeMethodModifierEvent.renamed);
		if (originalMethod instanceof AnnotableAndModifiable && changedMethod instanceof AnnotableAndModifiable) {
			final AnnotableAndModifiable originalModifiable = (AnnotableAndModifiable) originalMethod;
			final AnnotableAndModifiable changedModifiable = (AnnotableAndModifiable) changedMethod;
			final CompositeContainerChange change = this.buildModifierChanges(originalMethod, changedMethod,
					originalModifiable.getModifiers(), changedModifiable.getModifiers(),
					changeMethodModifierEvent.original);
			return Optional.of(change);
		} else {
			logger.info(
					"ChangeMethodModifiersEvent type could not be reported. Either original or changed is not instanceof Modifiable: orginal: "
							+ originalMethod + " changed: " + changedMethod);
			return Optional.empty();
		}
	}

	@Override
	public Optional<VitruviusChange> visit(final ChangeClassModifiersEvent changeClassModifiersEvent) {
		return this.handleClassifierModifierChanges(changeClassModifiersEvent.original,
				changeClassModifiersEvent.changed);
	}

	@Override
	public Optional<VitruviusChange> visit(final ChangeInterfaceModifiersEvent changeInterfaceModifiersEvent) {
		return this.handleClassifierModifierChanges(changeInterfaceModifiersEvent.original,
				changeInterfaceModifiersEvent.changed);
	}

	private Optional<VitruviusChange> handleClassifierModifierChanges(final TypeDeclaration original,
			final TypeDeclaration changed) {
		final CompilationUnitAdapter originalCU = this.util.getUnsavedCompilationUnitAdapter(original);
		final ConcreteClassifier originalClassifier = originalCU.getConcreteClassifierForTypeDeclaration(original);
		final CompilationUnitAdapter changedCU = this.util.getUnsavedCompilationUnitAdapter(changed);
		final ConcreteClassifier changedClassifier = changedCU.getConcreteClassifierForTypeDeclaration(changed);

		final CompositeContainerChange change = this.buildModifierChanges(originalClassifier, changedClassifier,
				originalClassifier.getModifiers(), changedClassifier.getModifiers(), original);
		return Optional.of(change);
	}

	private CompositeContainerChange buildModifierChanges(final EObject modifiableBeforeChange,
			final EObject modifiableAfterChange, final List<Modifier> oldModifiers, final List<Modifier> newModifiers,
			final ASTNode oldNode) {
		final List<Modifier> originalModifiers = new ArrayList<Modifier>(oldModifiers);
		final List<Modifier> changedModifiers = new ArrayList<Modifier>(newModifiers);

		for (final Modifier changedModifier : newModifiers) {
			for (final Modifier origModifier : oldModifiers) {
				if (changedModifier.getClass() == origModifier.getClass()) {
					originalModifiers.remove(origModifier);
					changedModifiers.remove(changedModifier);
					break;
				}
			}
		}

		final CompositeContainerChange modifierChanges = VitruviusChangeFactory.getInstance()
				.createCompositeContainerChange();
		for (final Modifier removedModifier : originalModifiers) {
			final EChange eChange = JamoppChangeBuildHelper.createRemoveAnnotationOrModifierChange(removedModifier,
					modifiableAfterChange);
			modifierChanges.addChange(this.util.wrapToVitruviusModelChange(eChange, oldNode));
		}
		for (final Modifier newModifier : changedModifiers) {
			final EChange eChange = JamoppChangeBuildHelper.createAddAnnotationOrModifierChange(newModifier,
					modifiableBeforeChange);
			modifierChanges.addChange(this.util.wrapToVitruviusModelChange(eChange, oldNode));
		}
		return modifierChanges;
	}

	@Override
	public Optional<VitruviusChange> visit(final AddFieldEvent addFieldEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(addFieldEvent.typeBeforeAdd);
		final ConcreteClassifier classifierBeforeAdd = originalCU
				.getConcreteClassifierForTypeDeclaration(addFieldEvent.typeBeforeAdd);
		final CompilationUnitAdapter changedCU = this.util.getUnsavedCompilationUnitAdapter(addFieldEvent.field);
		final Field field = changedCU.getFieldForVariableDeclarationFragment(addFieldEvent.fieldFragment);
		final EChange eChange = JamoppChangeBuildHelper.createAddFieldChange(field, classifierBeforeAdd);
		return this.util.createVitruviusChange(eChange, addFieldEvent.field);
	}

	@Override
	public Optional<VitruviusChange> visit(final RemoveFieldEvent removeFieldEvent) {
		final CompilationUnitAdapter originalCU = this.util.getUnsavedCompilationUnitAdapter(removeFieldEvent.field);
		final Field field = originalCU.getFieldForVariableDeclarationFragment(removeFieldEvent.fieldFragment);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(removeFieldEvent.typeAfterRemove);
		final ConcreteClassifier classiferAfterRemove = changedCU
				.getConcreteClassifierForTypeDeclaration(removeFieldEvent.typeAfterRemove);
		final EChange eChange = JamoppChangeBuildHelper.createAddFieldChange(field, classiferAfterRemove);
		return this.util.createVitruviusChange(eChange, removeFieldEvent.field);
	}

	@Override
	public Optional<VitruviusChange> visit(final ChangeFieldModifiersEvent changeFieldModifiersEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(changeFieldModifiersEvent.original);
		final List<Field> originalFields = originalCU.getFieldsForFieldDeclaration(changeFieldModifiersEvent.original);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(changeFieldModifiersEvent.changed);
		final List<Field> changedFields = changedCU.getFieldsForFieldDeclaration(changeFieldModifiersEvent.changed);

		final CompositeContainerChange allFieldModifierChanges = VitruviusChangeFactory.getInstance()
				.createCompositeContainerChange();
		final ListIterator<Field> ofit = originalFields.listIterator();
		while (ofit.hasNext()) {
			final Field oField = ofit.next();
			final ListIterator<Field> cfit = changedFields.listIterator();
			while (cfit.hasNext()) {
				final Field cField = cfit.next();
				if (oField.getName().equals(cField.getName())) {
					cfit.remove();
					final CompositeContainerChange fieldModifierChanges = this.buildModifierChanges(oField, cField,
							oField.getModifiers(), cField.getModifiers(), changeFieldModifiersEvent.original);
					allFieldModifierChanges.addChange(fieldModifierChanges);
				}
			}
		}
		return Optional.of(allFieldModifierChanges);
	}

	@Override
	public Optional<VitruviusChange> visit(final ChangeFieldTypeEvent changeFieldTypeEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(changeFieldTypeEvent.original);
		final List<Field> originalFields = originalCU.getFieldsForFieldDeclaration(changeFieldTypeEvent.original);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(changeFieldTypeEvent.changed);
		final List<Field> changedFields = changedCU.getFieldsForFieldDeclaration(changeFieldTypeEvent.changed);

		final CompositeContainerChange typeChanges = VitruviusChangeFactory.getInstance()
				.createCompositeContainerChange();
		final ListIterator<Field> ofit = originalFields.listIterator();
		while (ofit.hasNext()) {
			final Field oField = ofit.next();
			final ListIterator<Field> cfit = changedFields.listIterator();
			while (cfit.hasNext()) {
				final Field cField = cfit.next();
				if (oField.getName().equals(cField.getName())) {
					cfit.remove();
					final EChange eChange = JamoppChangeBuildHelper.createChangeFieldTypeChange(oField, cField);
					typeChanges.addChange(this.util.wrapToVitruviusModelChange(eChange, changeFieldTypeEvent.original));
				}
			}
		}
		return Optional.of(typeChanges);
	}

	@Override
	public Optional<VitruviusChange> visit(final AddAnnotationEvent addAnnotationEvent) {
		logger.info("React to AddMethodAnnotationEvent");
		final CompilationUnitAdapter oldCU = this.util
				.getUnsavedCompilationUnitAdapter(addAnnotationEvent.bodyDeclaration);
		final AnnotableAndModifiable annotableAndModifiable = oldCU
				.getAnnotableAndModifiableForBodyDeclaration(addAnnotationEvent.bodyDeclaration);

		final CompilationUnitAdapter newCu = this.util.getUnsavedCompilationUnitAdapter(addAnnotationEvent.annotation);
		final AnnotationInstance annotationInstance = newCu.getAnnotationInstanceForMethodAnnotation(
				addAnnotationEvent.annotation, addAnnotationEvent.bodyDeclaration);
		if (null != annotationInstance) {
			final EChange eChange = JamoppChangeBuildHelper.createAddAnnotationOrModifierChange(annotationInstance,
					annotableAndModifiable);
			return this.util.createVitruviusChange(eChange, addAnnotationEvent.annotation);
		}
		return Optional.empty();
	}

	@Override
	public Optional<VitruviusChange> visit(final RemoveAnnotationEvent removeAnnotationEvent) {
		final CompilationUnitAdapter cuWithAnnotation = this.util
				.getUnsavedCompilationUnitAdapter(removeAnnotationEvent.annotation);
		final AnnotationInstance removedAnnotation = cuWithAnnotation.getAnnotationInstanceForMethodAnnotation(
				removeAnnotationEvent.annotation, removeAnnotationEvent.bodyAfterChange);
		if (null != removedAnnotation) {
			final CompilationUnitAdapter cuWithoutAnnotation = this.util
					.getUnsavedCompilationUnitAdapter(removeAnnotationEvent.bodyAfterChange);
			final AnnotableAndModifiable annotableAndModifiable = cuWithoutAnnotation
					.getAnnotableAndModifiableForBodyDeclaration(removeAnnotationEvent.bodyAfterChange);
			final EChange eChange = JamoppChangeBuildHelper.createRemoveAnnotationOrModifierChange(removedAnnotation,
					annotableAndModifiable);
			return this.util.createVitruviusChange(eChange, removeAnnotationEvent.annotation);
		}
		return Optional.empty();
	}

	@Override
	public Optional<VitruviusChange> visit(final RenamePackageEvent renamePackageEvent) {
		final EChange renamePackageChange = JamoppChangeBuildHelper.createRenamePackageChange(
				renamePackageEvent.originalPackageName, renamePackageEvent.renamedPackageName);
		return this.util.createVitruviusModelChange(renamePackageChange, renamePackageEvent.originalIResource);
	}

	@Override
	public Optional<VitruviusChange> visit(final DeletePackageEvent deletePackageEvent) {
		final EChange deletePackageChange = JamoppChangeBuildHelper
				.createDeletePackageChange(deletePackageEvent.packageName);
		return this.util.createVitruviusModelChange(deletePackageChange, deletePackageEvent.iResource);
	}

	@Override
	public Optional<VitruviusChange> visit(final CreatePackageEvent addPackageEvent) {
		final EChange createPackageChange = JamoppChangeBuildHelper
				.createCreatePackageChange(addPackageEvent.packageName);
		return this.util.createVitruviusModelChange(createPackageChange, addPackageEvent.iResource);
	}

	@Override
	public Optional<VitruviusChange> visit(final RenameParameterEvent renameParameterEvent) {
		final CompilationUnitAdapter originalCU = this.util
				.getUnsavedCompilationUnitAdapter(renameParameterEvent.original);
		final Parameter original = originalCU.getParameterForVariableDeclaration(renameParameterEvent.originalParam);
		final URI uri = this.util.getFirstExistingURI(renameParameterEvent.original, renameParameterEvent.renamed);
		final CompilationUnitAdapter changedCU = this.util
				.getUnsavedCompilationUnitAdapter(renameParameterEvent.renamed, uri);
		final Parameter renamed = changedCU.getParameterForVariableDeclaration(renameParameterEvent.changedParam);
		final EChange eChange = JamoppChangeBuildHelper.createRenameParameterChange(original, renamed);
		return this.util.createVitruviusChange(eChange, renameParameterEvent.original);
	}

	@Override
	public Optional<VitruviusChange> visit(final RenamePackageDeclarationEvent renamePackageDeclarationEvent) {
		logger.warn("RenamePackageDeclarationEvent not supported yet");
		return Optional.empty();
	}

	@Override
	public Optional<VitruviusChange> visit(final ChangePackageDeclarationEvent changePackageDeclarationEvent) {
		logger.warn("ChangePackageDeclarationEvent not supported yet");
		return Optional.empty();
	}

	@Override
	public Optional<VitruviusChange> visit(final AddJavaDocEvent addJavaDocEvent) {
		return Optional.empty();
	}

	@Override
	public Optional<VitruviusChange> visit(final RemoveJavaDocEvent removeJavaDocEvent) {
		return Optional.empty();
	}

	@Override
	public Optional<VitruviusChange> visit(final ChangeJavaDocEvent changeJavaDocEvent) {
		return Optional.empty();
	}

	protected final class ChangeResponderUtility {

		private ChangeResponderUtility() {
		}

		public CompilationUnitAdapter getUnsavedCompilationUnitAdapter(final ASTNode astNode) {
			final URI uri = this.getURIFromCompilationUnit(astNode);
			return this.getUnsavedCompilationUnitAdapter(astNode, uri);
		}

		public CompilationUnitAdapter getUnsavedCompilationUnitAdapter(final ASTNode astNode, final URI uri) {
			CompilationUnitAdapter cu = null;
			if (null == astNode) {
				return null;
			}
			cu = new CompilationUnitAdapter(astNode, uri, false);
			if (cu.getCompilationUnit() == null) {
				cu = null;
			}
			return cu;
		}

		private Optional<VitruviusChange> createVitruviusChange(final EChange eChange,
				final ASTNode astNodeWithIResource) {
			final ConcreteChange change = this.wrapToVitruviusModelChange(eChange, astNodeWithIResource);
			return Optional.of(change);
		}

		private Optional<VitruviusChange> createVitruviusModelChange(final EChange eChange,
				final IResource originalIResource) {
			final ConcreteChange change = this.wrapToVitruviusModelChange(eChange, originalIResource);
			return Optional.of(change);
		}

		private ConcreteChange wrapToVitruviusModelChange(final EChange eChange, final ASTNode astNodeWithIResource) {
			final VURI vuri = VURI.getInstance(AST2Jamopp.getIResource(astNodeWithIResource));
			return VitruviusChangeFactory.getInstance().createConcreteChangeWithVuri(eChange, vuri);
		}

		private ConcreteChange wrapToVitruviusModelChange(final EChange eChange, final IResource originalIResource) {
			final VURI vuri = VURI.getInstance(originalIResource);
			return VitruviusChangeFactory.getInstance().createConcreteChangeWithVuri(eChange, vuri);
		}

		// returns URI from node1 if exists, otherwise URI from node2 or null if both
		// have no attached IResource
		URI getFirstExistingURI(final ASTNode node1, final ASTNode node2) {
			URI uri = this.getURIFromCompilationUnit(node1);
			if (uri == null) {
				uri = this.getURIFromCompilationUnit(node2);
			}
			return uri;
		}

		private URI getURIFromCompilationUnit(final ASTNode astNode) {
			// TODO IPath for CompilationUnit without linked IResource
			// IPath iPath = AST2JaMoPP.getIPathFromCompilationUnitWithResource(astNode);
			final IResource iResource = AST2Jamopp.getIResource(astNode);
			if (null == iResource) {
				return null;
			}
			final IPath iPath = iResource.getFullPath();
			if (iPath == null) {
				return null;
			}
			return URI.createPlatformResourceURI(iPath.toString(), true);
		}
	}
}
