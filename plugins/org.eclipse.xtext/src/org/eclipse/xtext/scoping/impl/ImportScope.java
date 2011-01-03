/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.scoping.impl;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.ISelectable;
import org.eclipse.xtext.resource.impl.AliasedEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.Selectors;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * @author Sven Efftinge - Initial contribution and API
 * @author Sebastian Zarnekow
 */
public class ImportScope extends AbstractScope {

	private final List<ImportNormalizer> normalizers;

	private final ISelectable importFrom;

	private final EClass type;

	public ImportScope(List<ImportNormalizer> namespaceResolvers, IScope parent, ISelectable importFrom, EClass type,
			boolean ignoreCase) {
		super(parent, ignoreCase);
		this.type = type;
		this.normalizers = removeDuplicates(namespaceResolvers);
		this.importFrom = importFrom;
	}

	protected List<ImportNormalizer> removeDuplicates(List<ImportNormalizer> namespaceResolvers) {
		ArrayList<ImportNormalizer> list = newArrayList();
		for (ImportNormalizer importNormalizer : namespaceResolvers) {
			if (!list.contains(importNormalizer))
				list.add(importNormalizer);
		}
		return list;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + normalizers + getAllLocalElements() + " -> " + getParent();
	}

	@Override
	protected Iterable<IEObjectDescription> getAllElements() {
		final Iterable<IEObjectDescription> globalElements = getParent().getElements(Selectors.selectAll());
		Iterable<IEObjectDescription> aliased = getAllLocalElements();
		final Map<QualifiedName, IEObjectDescription> elements = Maps.uniqueIndex(aliased,
				new Function<IEObjectDescription, QualifiedName>() {
					public QualifiedName apply(IEObjectDescription from) {
						return isIgnoreCase() ? from.getName().toLowerCase() : from.getName();
					}
				});
		return concat(aliased, filter(globalElements, new Predicate<IEObjectDescription>() {
			public boolean apply(IEObjectDescription input) {
				return !elements.containsKey(isIgnoreCase() ? input.getName().toLowerCase() : input.getName());
			}
		}));
	}

	@Override
	protected Iterable<IEObjectDescription> getAllLocalElements() {
		final Iterable<IEObjectDescription> exportedObjects = getImportFrom().getExportedObjects();
		return getAliasedElements(exportedObjects);
	}

	@Override
	protected Iterable<IEObjectDescription> getLocalElementsByEObject(final EObject object, final URI uri) {
		Iterable<IEObjectDescription> candidates = getImportFrom().getExportedObjectsByObject(object);
		return getAliasedElements(candidates);
	}

	/*
	 * public for testing purposes only
	 */
	protected Iterable<IEObjectDescription> getAliasedElements(Iterable<IEObjectDescription> candidates) {
		Multimap<QualifiedName, IEObjectDescription> keyToDescription = ArrayListMultimap.create();
		Multimap<QualifiedName, ImportNormalizer> keyToNormalizer = HashMultimap.create();

		for (IEObjectDescription imported : candidates) {
			QualifiedName fullyQualifiedName = imported.getName();
			for (ImportNormalizer normalizer : normalizers) {
				QualifiedName alias = normalizer.deresolve(fullyQualifiedName);
				if (alias != null) {
					QualifiedName key = alias;
					if (isIgnoreCase()) {
						key = key.toLowerCase();
					}
					keyToDescription.put(key, new AliasedEObjectDescription(alias, imported));
					keyToNormalizer.put(key, normalizer);
				}
			}
		}
		for (QualifiedName name : keyToNormalizer.keySet()) {
			if (keyToNormalizer.get(name).size() > 1)
				keyToDescription.removeAll(name);
		}
		return keyToDescription.values();
	}

	@Override
	protected IEObjectDescription getSingleLocalElementByName(QualifiedName name) {
		Iterator<IEObjectDescription> iterator = getLocalElementsByName(name).iterator();
		return iterator.hasNext() ? iterator.next() : null;
	}

	@Override
	protected Iterable<IEObjectDescription> getLocalElementsByName(QualifiedName name) {
		List<IEObjectDescription> result = newArrayList();
		ImportNormalizer foundForNormalizer = null;
		ISelectable importFrom = getImportFrom();
		for (ImportNormalizer normalizer : normalizers) {
			final QualifiedName resolvedName = normalizer.resolve(name);
			if (resolvedName != null) {
				Iterable<IEObjectDescription> resolvedElements = importFrom.getExportedObjects(type, resolvedName,
						isIgnoreCase());
				for (IEObjectDescription resolvedElement : resolvedElements) {
					if (foundForNormalizer == null)
						foundForNormalizer = normalizer;
					else if (foundForNormalizer != normalizer)
						return emptyList();
					QualifiedName alias = normalizer.deresolve(resolvedElement.getName());
					if (alias == null)
						throw new IllegalStateException("Couldn't deresolve " + resolvedElement.getName()
								+ " with import " + normalizer);
					final AliasedEObjectDescription aliasedEObjectDescription = new AliasedEObjectDescription(alias,
							resolvedElement);
					result.add(aliasedEObjectDescription);
				}
			}
		}
		return result;
	}

	protected ISelectable getImportFrom() {
		ISelectable importFrom = this.importFrom;
		if (importFrom == null) {
			importFrom = new ScopeBasedSelectable(getParent());
		}
		return importFrom;
	}

}
