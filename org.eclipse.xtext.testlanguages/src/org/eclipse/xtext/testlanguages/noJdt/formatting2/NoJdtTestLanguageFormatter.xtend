/*
 * generated by Xtext
 */
package org.eclipse.xtext.testlanguages.noJdt.formatting2

import org.eclipse.xtext.formatting2.AbstractFormatter2
import org.eclipse.xtext.formatting2.IFormattableDocument
import org.eclipse.xtext.testlanguages.noJdt.noJdt.Greeting
import org.eclipse.xtext.testlanguages.noJdt.noJdt.Model

class NoJdtTestLanguageFormatter extends AbstractFormatter2 {
	
	def dispatch void format(Model model, extension IFormattableDocument document) {
		// TODO: format HiddenRegions around keywords, attributes, cross references, etc. 
		for (Greeting greeting : model.getGreetings()) {
			greeting.format;
		}
	}
	
	// TODO: implement for 
}
