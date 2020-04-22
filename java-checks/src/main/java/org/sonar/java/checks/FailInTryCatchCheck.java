/*
 * SonarQube Java
 * Copyright (C) 2012-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.java.model.ExpressionUtils;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.CatchTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TryStatementTree;
import org.sonar.plugins.java.api.tree.VariableTree;

@Rule(key = "S5779")
public class FailInTryCatchCheck extends IssuableSubscriptionVisitor {

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.singletonList(Tree.Kind.TRY_STATEMENT);
  }

  @Override
  public void visitNode(Tree tree) {
    TryStatementTree tryStatementTree = (TryStatementTree) tree;

    getCatchAssertionErrorParameter(tryStatementTree.catches()).ifPresent(catchTree -> {
      TryBodyVisitor tryBodyVisitor = new TryBodyVisitor();
      tryStatementTree.block().accept(tryBodyVisitor);
      tryBodyVisitor.getFailIdentifierTree().ifPresent(failIdentifier -> reportWithSecondaryLocation(failIdentifier, catchTree));
    });
  }

  private static Optional<VariableTree> getCatchAssertionErrorParameter(List<CatchTree> catches) {
    return catches.stream()
      .map(CatchTree::parameter)
      .filter(param -> {
        Type symbolType = param.type().symbolType();
        return param.symbol().usages().isEmpty() &&
          (symbolType.isSubtypeOf("java.lang.AssertionError")
            || symbolType.is("java.lang.Error")
            || symbolType.is("java.lang.Throwable"));
      }).findFirst();
  }

  private void reportWithSecondaryLocation(IdentifierTree failIdentifier, VariableTree secondary) {
    reportIssue(failIdentifier,
      "Don't use fail() inside a try-catch catching an AssertionError.",
      Collections.singletonList(new JavaFileScannerContext.Location(
        "This parameter will catch the AssertionError thrown by fail()",
        secondary.type())),
      null);
  }

  private static class TryBodyVisitor extends BaseTreeVisitor {

    private static final MethodMatchers FAIL_MATCHER = MethodMatchers.create()
      .ofTypes("org.junit.Assert", "org.junit.jupiter.api.Assertions")
      .names("fail")
      .withAnyParameters()
      .build();

    private IdentifierTree failIdentifierTree = null;

    @Override
    public void visitMethodInvocation(MethodInvocationTree methodInvocation) {
      if (FAIL_MATCHER.matches(methodInvocation)) {
        failIdentifierTree = ExpressionUtils.methodName(methodInvocation);
      }
    }

    private Optional<IdentifierTree> getFailIdentifierTree() {
      if (failIdentifierTree == null) {
        return Optional.empty();
      } else {
        return Optional.of(failIdentifierTree);
      }
    }
  }

}
