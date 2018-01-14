package com.sirolf2009.gladiator

import org.eclipse.e4.core.contexts.IEclipseContext
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate
import org.eclipse.e4.ui.workbench.lifecycle.PreSave
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals

class E4LifeCycle {
	
	@PostContextCreate
	def void postContextCreate(IEclipseContext workbenchContext) {
	}

	@PreSave
	def void preSave(IEclipseContext workbenchContext) {
	}

	@ProcessAdditions
	def void processAdditions(IEclipseContext workbenchContext) {
	}

	@ProcessRemovals
	def void processRemovals(IEclipseContext workbenchContext) {
	}
}