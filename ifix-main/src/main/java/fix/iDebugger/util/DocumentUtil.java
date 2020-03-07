package fix.iDebugger.util;

import java.util.HashSet;

import fix.iDebugger.handlers.FixHub;
import fix.iDebugger.nodes.RepairNode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import edu.tamu.aser.tide.nodes.DLockNode;
import edu.tamu.aser.tide.nodes.MemNode;
import fix.iDebugger.nodes.RaceLockUNode;

public class DocumentUtil {

	public static boolean policyIsLockedBySuggestionLock(MemNode raceNode, RepairNode repairNode) {
		boolean hasLocked = false;
		FixHub fixHub = FixHub.getInstance();
		HashSet<DLockNode> currentLocks = fixHub.nodeToLocks.get(raceNode);
		if (currentLocks != null) {
			for (DLockNode lockNode : currentLocks) {
				if(repairNode.equals(RaceLockUNode.getNode(lockNode).getRepairNode())){
					hasLocked = true;
					break;
				}
			}
		}
		return hasLocked;
	}
	
	public static int ignoreComment(IDocument document, int cLine) {
		int line = cLine + 1;
		String currentText = document.get();
		try {
			IRegion statementInformation = null;
			String statement = null;

			while (true) {
				statementInformation = document.getLineInformation(line - 1);
				int statementStart = statementInformation.getOffset();
				int statementend = statementInformation.getOffset() + statementInformation.getLength();
				statement = currentText.substring(statementStart, statementend).trim();

				if (statement.startsWith("/*")) {
					if (statement.endsWith("*/")) {
						line++;
						continue;
					}
					do {
						line++;
						statementInformation = document.getLineInformation(line - 1);
						statementStart = statementInformation.getOffset();
						statementend = statementInformation.getOffset() + statementInformation.getLength();
						statement = currentText.substring(statementStart, statementend).trim();
					} while (!statement.endsWith("*/"));
					line++;
				} else if (statement.startsWith("//") || statement.length() == 0) {
					line++;
					continue;
				} else {
					break;
				}
			}
			line--;
			return line;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
