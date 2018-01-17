package com.sirolf2009.gladiator

import org.eclipse.jface.preference.FieldEditorPreferencePage
import org.eclipse.jface.preference.StringFieldEditor

class GladiatorPreferences extends FieldEditorPreferencePage {
	
	var StringFieldEditor apiKey
	var StringFieldEditor apiSecret
	var StringFieldEditor apiPassword
	
	new() {
		super("Authentication", GRID)
	}
	
	override protected createFieldEditors() {
		fieldEditorParent => [
			apiKey = new StringFieldEditor("API_KEY", "API Key", it)
			apiSecret = new StringFieldEditor("API_SECRET", "API Secret", it)
			apiPassword = new StringFieldEditor("API_PASSWORD", "API Password", it)
			addField(apiKey)
			addField(apiSecret)
			addField(apiPassword)
		]
	}
	
}