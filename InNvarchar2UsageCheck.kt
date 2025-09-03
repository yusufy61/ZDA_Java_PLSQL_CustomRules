/**
 * Z PL/SQL Analyzer
 * Copyright (C) 2015-2025 Felipe Zorzo
 * mailto:felipe AT felipezorzo DOT com DOT br
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
package com.felipebz.zpa.checks

import com.felipebz.flr.api.AstNode
import com.felipebz.zpa.api.PlSqlGrammar
import com.felipebz.zpa.api.PlSqlKeyword
import com.felipebz.zpa.api.annotations.*

@Rule(priority = Priority.MAJOR, tags = [Tags.PERFORMANCE])
@ConstantRemediation("5min")
@RuleInfo(scope = RuleInfo.Scope.ALL)
@ActivatedByDefault
class InNvarchar2UsageCheck : AbstractBaseCheck() {

    override fun init() {
        subscribeTo(PlSqlGrammar.PARAMETER_DECLARATION)
    }

    override fun visitNode(node: AstNode) {
        // Check if parameter has IN keyword (explicitly or implicitly)
        val hasExplicitIn = node.hasDirectChildren(PlSqlKeyword.IN)
        val hasOut = node.hasDirectChildren(PlSqlKeyword.OUT)
        
        // If no explicit OUT, then it's an IN parameter (implicitly or explicitly)
        if (!hasOut) {
            // Find the DATATYPE node
            val datatypeNode = node.getFirstChild(PlSqlGrammar.DATATYPE)
            if (datatypeNode != null) {
                // Check if datatype contains NVARCHAR2
                val nvarchar2Node = findNvarchar2Node(datatypeNode)
                if (nvarchar2Node != null) {
                    addIssue(nvarchar2Node, getLocalizedMessage())
                }
            }
        }
    }

    private fun findNvarchar2Node(datatypeNode: AstNode): AstNode? {
        // Recursively search for NVARCHAR2 keyword in the datatype tree
        return findNodeByTokenValue(datatypeNode, "nvarchar2")
    }

    private fun findNodeByTokenValue(node: AstNode, targetValue: String): AstNode? {
        // Check current node's token value (case-insensitive)
        if (node.tokenValue?.equals(targetValue, ignoreCase = true) == true) {
            return node
        }
        
        // Recursively check all children
        for (child in node.children) {
            val result = findNodeByTokenValue(child, targetValue)
            if (result != null) {
                return result
            }
        }
        
        return null
    }
}
