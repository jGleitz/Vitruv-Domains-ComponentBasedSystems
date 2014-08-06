package edu.kit.ipd.sdq.vitruvius.framework.run.editor.monitored.emfchange.test.mocking;

import java.net.URISyntaxException;

import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.junit.Before;
import org.junit.Test;

import edu.kit.ipd.sdq.vitruvius.framework.run.editor.monitored.emfchange.test.mocking.MockEditingDomainFactory;
import edu.kit.ipd.sdq.vitruvius.framework.run.editor.monitored.emfchange.test.testmodels.Files;
import edu.kit.ipd.sdq.vitruvius.framework.run.editor.monitored.emfchange.test.utils.EnsureExecuted;

public class MockEditingDomainFactoryTests {
    private MockEditingDomainFactory factory;
    
    @Before
    public void setUp() {
        this.factory = new MockEditingDomainFactory();
    }
    
    @Test
    public void createResourceSet() throws URISyntaxException {
        EditingDomain ed = factory.createEditingDomain(Files.EXAMPLEMODEL_ECORE);
        ResourceSet rs = ed.getResourceSet();
        assert rs != null;
        assert rs.getResources().size() == 1;
        Resource r = rs.getResources().get(0);
        assert r.getContents().get(0) instanceof EPackage;
    }
    
    @Test
    public void createEditingDomain() {
        EditingDomain ed = factory.createEditingDomain(Files.EXAMPLEMODEL_ECORE);
        assert ed != null;
        assert ed.getRoot(EcoreFactory.eINSTANCE.createEClass()) != null;
        assert ed.getResourceSet() != null;
        
        ResourceSet rs = ed.getResourceSet();
        assert rs.getResources().size() == 1;
        assert rs.getResources().get(0).getContents().get(0) == ed.getRoot(EcoreFactory.eINSTANCE.createEClass());
    }
    
    @Test
    public void createCommandStack() {
        TransactionalEditingDomain ed = factory.createEditingDomain(Files.EXAMPLEMODEL_ECORE);
        CommandStack cs = ed.getCommandStack();
        assert cs != null;
        
        final EnsureExecuted ensureExecuted = new EnsureExecuted();
        
        cs.execute(new RecordingCommand(ed) {
            @Override
            protected void doExecute() {
                ensureExecuted.markExecuted();
            }
        });
        
        assert !ensureExecuted.isIndicatingFail() : "Did not execute the listener.";
    }
}