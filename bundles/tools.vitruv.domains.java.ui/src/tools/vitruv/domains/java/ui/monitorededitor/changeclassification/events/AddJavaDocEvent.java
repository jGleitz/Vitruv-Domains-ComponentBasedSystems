package tools.vitruv.domains.java.ui.monitorededitor.changeclassification.events;

import org.eclipse.jdt.core.dom.Javadoc;

import tools.vitruv.domains.java.ui.monitorededitor.changeclassification.ChangeEventVisitor;

public class AddJavaDocEvent extends JavaDocEvent {

	public AddJavaDocEvent(Javadoc comment) {
		super(comment);
	}

	@Override
	public String toString() {
		return "AddJavaDocEvent [comment=" + this.comment.toString() + "]";
	}

	@Override
	public <T> T accept(final ChangeEventVisitor<T> visitor) {
		return visitor.visit(this);
	}

}
