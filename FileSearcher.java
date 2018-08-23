/*
	An example that uses the fork/join framework meant to encourage the partititioning of a
   problem into subproblems, which threads executing on separate processors handling
   each subproblem.

    The program searches from the user's home directory for
    files extending with '.txt' as the extension. The list of
    such files is printed to the screen.
*/

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.RecursiveTask;

public class FileSearcher extends RecursiveTask<List<String>> {
	private final String path;
	private final String extension;

	public FileSearcher(String path, String extension) {
		this.path = path;
		this.extension = extension;
	}

	@Override
	public List<String> compute() {
		List<String> listOfFileNames = new ArrayList<String>();
		File[] files = new File(path).listFiles();
		if(files == null) return listFileNames;		// base case

		List<FileSearcher> tasks = new ArrayList<FileSearcher>();
		for (File file : files) {
			String absolutePath = file.getAbsolutePath();
			if (file.isDirectory()) {
				FileSearcher task = new FileSearcher(absolutePath, extension);
				task.fork();	// recursive case, due to fork framework
				tasks.add(task);
			} else if (file.getName().endsWith(extension)) {
				listOfFileNames.add(absolutePath);
			}
		}
		assembleResults(listOfFileNames, tasks);
		return listOfFileNames;
	}

	private void assembleResults(List<String> list, List<FileSearcher> tasks) {
		for (FileSearcher task : tasks) {
			Collection<String> results = task.join();
			list.addAll(results);
		}
	}
}

class MainFS {
	public static void main(String[] args) {
		List<String> list = new FileSearcher(System.getProperty("user.home"), ".txt").compute();
		System.out.println("Files with ext .txt:");
		for (String string : list) { System.out.println(string); }
	}
}