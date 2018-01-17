package com.sirolf2009.gladiator.handlers

import com.sirolf2009.gladiator.GladiatorPreferences
import org.eclipse.e4.core.di.annotations.Execute
import org.eclipse.jface.preference.PreferenceDialog
import org.eclipse.jface.preference.PreferenceManager
import org.eclipse.jface.preference.PreferenceNode
import org.eclipse.jface.preference.PreferenceStore
import org.eclipse.swt.widgets.Shell

class PreferenceHandler {

	@Execute
	def void execute(Shell shell) {
		val auth = new PreferenceNode("Authentication", new GladiatorPreferences())

		val manager = new PreferenceManager() => [
			addToRoot(auth)
		]

		val store = new PreferenceStore("gladiator.properties") => [
			try {
				load()
			} catch(Exception e) {
				e.printStackTrace()
			}
		]

		new PreferenceDialog(shell, manager) => [
			preferenceStore = store
			open()
			store.save()
		]
	}

}
