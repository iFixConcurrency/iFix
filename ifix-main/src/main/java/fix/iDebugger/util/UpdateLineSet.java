package fix.iDebugger.util;

import java.util.Set;

public class UpdateLineSet {

	private static Set<UpdateLine> upSet = new java.util.HashSet<UpdateLine>();

	public static int hasUpdated(String packageName, String className) {
		for (UpdateLine update : upSet) {
			if (update.isSameFile(packageName, className)) {
				return update.getUpdtaeNumber();
			}
		}
		return 0;
	}

	public static void addAndUpdate(UpdateLine updateLine) {
		if (!upSet.contains(updateLine)) {
			updateLine.update();
			upSet.add(updateLine);
		} else {
			for (UpdateLine ul : upSet) {
				if (ul.equals(updateLine)) {
					ul.update();
					break;
				}
			}
		}
	}

	public static Set<UpdateLine> getUpList() {
		return upSet;
	}

	public static void clear() {
		upSet.clear();
	}

}
