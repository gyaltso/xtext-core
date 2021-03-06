/**
 * Copyright (c) 2016 itemis AG (http://www.itemis.com) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.ide.tests.testlanguage.formatting2;

import com.google.common.collect.Iterables;
import java.util.Arrays;
import javax.inject.Inject;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.formatting2.AbstractFormatter2;
import org.eclipse.xtext.formatting2.IFormattableDocument;
import org.eclipse.xtext.formatting2.IHiddenRegionFormatter;
import org.eclipse.xtext.formatting2.regionaccess.ISemanticRegion;
import org.eclipse.xtext.ide.tests.testlanguage.services.TestLanguageGrammarAccess;
import org.eclipse.xtext.ide.tests.testlanguage.testLanguage.AbstractElement;
import org.eclipse.xtext.ide.tests.testlanguage.testLanguage.Model;
import org.eclipse.xtext.ide.tests.testlanguage.testLanguage.Property;
import org.eclipse.xtext.ide.tests.testlanguage.testLanguage.TypeDeclaration;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * @author Christian Dietrich - Initial contribution and API
 */
@SuppressWarnings("all")
public class TestLanguageFormatter extends AbstractFormatter2 {
  @Inject
  @Extension
  private TestLanguageGrammarAccess _testLanguageGrammarAccess;
  
  protected void _format(final Model model, @Extension final IFormattableDocument document) {
    EList<AbstractElement> _elements = model.getElements();
    for (final AbstractElement type : _elements) {
      document.<AbstractElement>format(type);
    }
  }
  
  protected void _format(final TypeDeclaration type, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.newLine();
    };
    document.append(this.textRegionExtensions.regionFor(type).keyword(this._testLanguageGrammarAccess.getTypeDeclarationAccess().getLeftCurlyBracketKeyword_3()), _function);
    final Procedure1<IHiddenRegionFormatter> _function_1 = (IHiddenRegionFormatter it) -> {
      it.newLine();
    };
    final Procedure1<IHiddenRegionFormatter> _function_2 = (IHiddenRegionFormatter it) -> {
      it.newLine();
    };
    document.append(document.prepend(this.textRegionExtensions.regionFor(type).keyword(this._testLanguageGrammarAccess.getTypeDeclarationAccess().getRightCurlyBracketKeyword_5()), _function_1), _function_2);
    final Procedure1<IHiddenRegionFormatter> _function_3 = (IHiddenRegionFormatter it) -> {
      it.indent();
    };
    document.<ISemanticRegion, ISemanticRegion>interior(
      this.textRegionExtensions.regionFor(type).keyword(this._testLanguageGrammarAccess.getTypeDeclarationAccess().getLeftCurlyBracketKeyword_3()), 
      this.textRegionExtensions.regionFor(type).keyword(this._testLanguageGrammarAccess.getTypeDeclarationAccess().getRightCurlyBracketKeyword_5()), _function_3);
    Iterable<Property> _filter = Iterables.<Property>filter(type.getMembers(), Property.class);
    for (final Property property : _filter) {
      document.<Property>format(property);
    }
  }
  
  protected void _format(final Property property, @Extension final IFormattableDocument document) {
    final Procedure1<IHiddenRegionFormatter> _function = (IHiddenRegionFormatter it) -> {
      it.newLine();
    };
    document.<Property>append(property, _function);
  }
  
  public void format(final Object property, final IFormattableDocument document) {
    if (property instanceof XtextResource) {
      _format((XtextResource)property, document);
      return;
    } else if (property instanceof Property) {
      _format((Property)property, document);
      return;
    } else if (property instanceof TypeDeclaration) {
      _format((TypeDeclaration)property, document);
      return;
    } else if (property instanceof Model) {
      _format((Model)property, document);
      return;
    } else if (property instanceof EObject) {
      _format((EObject)property, document);
      return;
    } else if (property == null) {
      _format((Void)null, document);
      return;
    } else if (property != null) {
      _format(property, document);
      return;
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(property, document).toString());
    }
  }
}
