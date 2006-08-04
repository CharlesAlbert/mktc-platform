package org.marketcetera.photon.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.marketcetera.core.ClassVersion;
import org.marketcetera.photon.CommandStatusLineContribution;
import org.marketcetera.photon.IImageKeys;
import org.marketcetera.photon.PhotonPlugin;

@ClassVersion("$Id$")
public class FocusCommandAction extends Action implements ISelectionListener,
		IWorkbenchAction {
	public final static String ID = "org.marketcetera.photon.FocusCommand";

	CommandStatusLineContribution commandInput;
	
	public FocusCommandAction(IWorkbenchWindow window, CommandStatusLineContribution commandInput) {
		setId(ID);
		setActionDefinitionId(ID);
		setText("Goto &command input area");
		setToolTipText("Put the cursor in the command input area");
		setImageDescriptor(PhotonPlugin.getImageDescriptor(IImageKeys.LIGHTNING));
		this.commandInput = commandInput;
	}

	public void dispose() {
	}

	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
	}

	public void run() {
		commandInput.setFocus();
	}

}
