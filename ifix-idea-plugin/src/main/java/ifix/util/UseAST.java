package ifix.util;


import org.eclipse.jdt.core.dom.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UseAST {

	public static int statementStart = 0;
	public static int statementEnd = 0;
	static List<UseASTNode> useASTNodeList = new ArrayList<UseASTNode>();
	static int end = 0;

	public static void main(String[] args) {
//		UseAST.useASTChangeLine(21, "D:\\t\\wrongLock\\WrongLock.java");
//		System.out.println("result" + UseAST.statementStart + "," + UseAST.statementEnd);
//		UseAST.useASTChangeLine(29, "D:\\t\\wrongLock\\WrongLock.java");
//		System.out.println("result" + UseAST.statementStart + "," + UseAST.statementEnd);
		System.out.println(UseAST.defInUseOut(6, 6, "D:\\Idea src\\Test\\src\\T2.java"));
	}

//	public static void useASTChangeLine(int addLoc, String filePath) {
//		useASTNodeList.clear();
//		statementStart = 0;
//		statementEnd = 0;
//		findNode(filePath);
//		for(UseASTNode node: useASTNodeList){
//			if(node.inNodeStartOrEnd(addLoc)){
//				statementStart = node.getStart();
//				statementEnd = node.getEnd();
//				break;
//			}
//		}
//	}

	public static void findNode(String filePath) {
		statementStart = 0;
		statementEnd = 0;
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(getFileContents(new File(filePath)));
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.accept(new ASTVisitor() {

//			@Override
//			public boolean visit(InfixExpression node) {
//				ASTNode parent = node.getParent();
//				int start = cu.getLineNumber(parent.getStartPosition());
//				int end = cu.getLineNumber(parent.getStartPosition() + parent.getLength());
//				if (start != end) {
//					UseASTNode usNode = new UseASTNode(start, end);
//					useASTNodeSet.add(usNode);
//				}
//				return super.visit(node);
//			}

			@Override
			public boolean visit(SwitchStatement node) {
				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());
				if (start != end) {
					UseASTNode usNode = new UseASTNode(start, end);
					useASTNodeList.add(usNode);
				}
				return super.visit(node);
			}

			@Override
            public boolean visit(IfStatement node) {
				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());
				if (start != end) {
					UseASTNode usNode = new UseASTNode(start, end);
					useASTNodeList.add(usNode);
				}
				return super.visit(node);
            }

			@Override
            public boolean visit(ForStatement node) {
				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());
				if (start != end) {
					UseASTNode usNode = new UseASTNode(start, end);
					useASTNodeList.add(usNode);
				}
				return super.visit(node);
            }

			@Override
            public boolean visit(WhileStatement node) {
				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());
				if (start != end) {
					UseASTNode usNode = new UseASTNode(start, end);
					useASTNodeList.add(usNode);
				}
				return super.visit(node);
            }

			@Override
            public boolean visit(DoStatement node) {
				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());
				if (start != end) {
					UseASTNode usNode = new UseASTNode(start, end);
					useASTNodeList.add(usNode);
				}
				return super.visit(node);
            }

			@Override
            public boolean visit(EnhancedForStatement node) {
				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());
				if (start != end) {
					UseASTNode usNode = new UseASTNode(start, end);
					useASTNodeList.add(usNode);
				}
				return super.visit(node);
            }

			@Override
            public boolean visit(ReturnStatement node) {
				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());
				if (start != end) {
					UseASTNode usNode = new UseASTNode(start, end);
					useASTNodeList.add(usNode);
				}
				return super.visit(node);
            }
		});
	}
	
	public static boolean getBeginEndOfStatement(int start, int end, String filePath){
		try{
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(new String(Files.readAllBytes(new File(filePath).toPath())).toCharArray());
			parser.setKind(ASTParser.K_COMPILATION_UNIT);

			statementStart = -1;
			statementEnd = -1;

			final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
			cu.accept(new ASTVisitor() {

				@Override
				public boolean visit(ReturnStatement node) {
					if (cu.getLineNumber(node.getStartPosition()) <= start && cu.getLineNumber(node.getStartPosition() + node.getLength()) >= end) {
						statementStart = cu.getLineNumber(node.getStartPosition());
						statementEnd = cu.getLineNumber(node.getStartPosition() + node.getLength());
					}
					return super.visit(node);
				}
			});

		}
		catch (IOException e){
			e.printStackTrace();
		}
		return statementStart  != -1;
	}
	
	public static int defInUseOut(int lockStart, int locEnd, String filePath) {
		try{
			statementStart = 0;
			statementEnd = 0;
			end = 0;
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setSource(new String(java.nio.file.Files.readAllBytes(new File(filePath).toPath())).toCharArray());
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

			cu.accept(new ASTVisitor() {

				//In the lock, what are the variables are defined
				Set<String> varDefInSync = new HashSet<String>();

				//The number of lines at the end of the function
				int functionEnd = 0;

				@Override
				public boolean visit(MethodDeclaration node) {
					if (cu.getLineNumber(node.getStartPosition()) <= lockStart && cu.getLineNumber(node.getStartPosition() + node.getLength()) >= locEnd) {
						this.functionEnd = cu.getLineNumber(node.getStartPosition() + node.getLength());
					}
					return super.visit(node);
				}

				@Override
				public boolean visit(VariableDeclarationFragment node) {
					if (cu.getLineNumber(node.getStartPosition()) >= lockStart && cu.getLineNumber(node.getStartPosition()) <= locEnd) {
						this.varDefInSync.add(node.getName().getIdentifier());
					}
					return super.visit(node);
				}

				@Override
				public boolean visit(SimpleName node) {
					if (this.varDefInSync.contains(node.getIdentifier())) {
						if (cu.getLineNumber(node.getStartPosition()) >= locEnd && cu.getLineNumber(node.getStartPosition() + node.getLength()) <= this.functionEnd) {
							end = cu.getLineNumber(node.getStartPosition() + node.getLength());
						}
					}

					return super.visit(node);
				}

			});
			return end;
		}
		catch (IOException e){
			e.printStackTrace();
			return -1;
		}
    }
	
	public static boolean crossBlock() {
		if (statementStart == statementEnd) {
			return false;
		} else {
			return true;
		}
	}

	// The file is processed into buffer array
	public static char[] getFileContents(File file) {
		// char array to store the file contents in
		char[] contents = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = br.readLine()) != null) {
				// Additional content and missing new lines.
				sb.append(line + "\n");
			}
			contents = new char[sb.length()];
			sb.getChars(0, sb.length() - 1, contents, 0);

			assert (contents.length > 0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return contents;
	}
}
