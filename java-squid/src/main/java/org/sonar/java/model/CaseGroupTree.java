/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.model;

import java.util.List;

/**
 * Group of 'case's in a 'switch' statement.
 *
 * JLS 14.11
 *
 * <pre>
 *   {@link #labels()} {@link #body()}
 * </pre>
 *
 * @since Java 1.3
 */
public interface CaseGroupTree extends Tree {

  List<? extends CaseLabelTree> labels();

  List<? extends StatementTree> body();

}
