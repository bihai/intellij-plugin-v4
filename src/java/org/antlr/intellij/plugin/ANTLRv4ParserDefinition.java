package org.antlr.intellij.plugin;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.antlr.intellij.plugin.adaptors.LexerAdaptor;
import org.antlr.intellij.plugin.adaptors.ParserAdaptor;
import org.antlr.intellij.plugin.adaptors.Utils;
import org.antlr.intellij.plugin.parser.ANTLRv4Lexer;
import org.antlr.intellij.plugin.parser.ANTLRv4Parser;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.jetbrains.annotations.NotNull;

/** The general interface between IDEA and ANTLR. All adaptor objects
 *  created here.  Try to create just one lexer, parser objects.
 */
public class ANTLRv4ParserDefinition implements ParserDefinition {
	public static final IFileElementType FILE =
		new IFileElementType(Language.<ANTLRv4Language>findInstance(ANTLRv4Language.class));

	@NotNull
	@Override
	public Lexer createLexer(Project project) {
		final ANTLRv4Lexer lexer = new ANTLRv4Lexer(null);
		LexerATNSimulator sim =
			Utils.getLexerATNSimulator(lexer, ANTLRv4Lexer._ATN, ANTLRv4Lexer._decisionToDFA,
									   ANTLRv4Lexer._sharedContextCache);
		lexer.setInterpreter(sim);
		return new LexerAdaptor(lexer);
	}

	@NotNull
	public PsiParser createParser(final Project project) {
		ANTLRv4Parser parser = new ANTLRv4Parser(null);
		return new ParserAdaptor(parser) {
			@Override
			public void parse(Parser parser, PsiBuilder builder) {
				((ANTLRv4Parser)parser).builder = builder;
				((ANTLRv4Parser) parser).file();
			}
		};
	}

	@NotNull
	public TokenSet getWhitespaceTokens() {
		return ANTLRv4TokenTypeAdaptor.WHITE_SPACES;
	}

	@NotNull
	public TokenSet getCommentTokens() {
		return ANTLRv4TokenTypeAdaptor.COMMENTS;
	}

	@NotNull
	public TokenSet getStringLiteralElements() {
		return TokenSet.EMPTY;
	}

	@Override
	public IFileElementType getFileNodeType() {
		return FILE;
	}

	@Override
	public PsiFile createFile(FileViewProvider viewProvider) {
		return new SimplePSIFileRoot(viewProvider);
	}

	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
		return SpaceRequirements.MAY;
	}

	/** Convert from parse node (AST they call it) to final PSI node. This
	 *  converts only internal rule nodes apparently, not leaf nodes. Leaves
	 *  are just tokens I guess.
	 */
	@NotNull
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
//		System.out.println("PSI createElement from "+elementType);
		if ( elementType == ANTLRv4TokenTypeAdaptor.ruleNameToIDEAElementType.get("func") ) {
			return new FuncElement(node);
		}
		return new SimplePSIElement(node);
	}
}
