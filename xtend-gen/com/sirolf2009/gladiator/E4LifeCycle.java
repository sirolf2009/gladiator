package com.sirolf2009.gladiator;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;

@SuppressWarnings("all")
public class E4LifeCycle {
  @PostContextCreate
  public void postContextCreate(final IEclipseContext workbenchContext) {
  }
  
  @PreSave
  public void preSave(final IEclipseContext workbenchContext) {
  }
  
  @ProcessAdditions
  public void processAdditions(final IEclipseContext workbenchContext) {
  }
  
  @ProcessRemovals
  public void processRemovals(final IEclipseContext workbenchContext) {
  }
}
