package uk.ac.warwick.dcs.sherlock.engine.storage.base;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.dcs.sherlock.api.common.ICodeBlockGroup;
import uk.ac.warwick.dcs.sherlock.api.common.ISourceFile;
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.Language;
import uk.ac.warwick.dcs.sherlock.engine.component.IWorkspace;
import uk.ac.warwick.dcs.sherlock.engine.component.WorkStatus;
import uk.ac.warwick.dcs.sherlock.engine.exception.WorkspaceUnsupportedException;
import uk.ac.warwick.dcs.sherlock.engine.storage.IStorageWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.*;
import java.util.zip.*;

public class BaseStorage implements IStorageWrapper {

	static BaseStorage instance;
	static Logger logger = LoggerFactory.getLogger(BaseStorage.class);

	EmbeddedDatabase database;
	BaseStorageFilesystem filesystem;

	public BaseStorage() {
		instance = this;

		this.database = new EmbeddedDatabase();
		this.filesystem = new BaseStorageFilesystem();

		//Do a scan of all files in database in background, check they exist and there are no extra files
		List orphans = this.filesystem.validateFileStore(this.database.runQuery("SELECT f from File f", EntityFile.class));
		if (orphans != null && orphans.size() > 0) {
			this.database.removeObject(orphans);
		}

		//list = this.database.runQuery("SELECT t from Task t", EntityTask.class).stream().filter(x -> x.getStatus() == WorkStatus.PREPARED).collect(Collectors.toList());
		List<EntityJob> jobs = this.database.runQuery("SELECT j from Job j", EntityJob.class);
		jobs.stream().filter(j -> j.getTasks().size() > 0 && j.getStatus() == WorkStatus.ACTIVE).forEach(j ->
		{
			if (j.getTasks().stream().anyMatch(i -> i.getStatus() == WorkStatus.PREPARED)) {
				j.getTasks().stream().filter(i -> i.getStatus() == WorkStatus.PREPARED).forEach(i -> ((EntityTask) i).setStatus(WorkStatus.INTERRUPTED));
				j.setStatus(WorkStatus.INTERRUPTED);
			}
		});

		jobs = jobs.stream().filter(j -> j.getTasks().size() == 0).collect(Collectors.toList());
		if (jobs.size() > 0) {
			logger.info("Removing jobs with no tasks...");
			this.database.removeObject(jobs);
		}
	}

	@Override
	public void close() {
		this.database.close();
	}

	@Override
	public IWorkspace createWorkspace(String name, Language lang) {
		IWorkspace w = new EntityWorkspace(name, lang);
		this.database.storeObject(w);
		return w;
	}

	@Override
	public Class<? extends ICodeBlockGroup> getCodeBlockGroupClass() {
		return EntityCodeBlockGroup.class;
	}

	@Override
	public List<IWorkspace> getWorkspaces(List<Long> ids) {
		return this.getWorkspaces().stream().filter(x -> ids.contains(x.getPersistentId())).collect(Collectors.toList());
	}

	@Override
	public List<IWorkspace> getWorkspaces() {
		List<EntityWorkspace> l = this.database.runQuery("SELECT w FROM Workspace w", EntityWorkspace.class);
		return new LinkedList<>(l);
	}

	@Override
	public ISourceFile getSourceFile(long persistentId) {
		List<EntityFile> f = this.database.runQuery("SELECT f FROM File f WHERE f.id=" + persistentId, EntityFile.class);
		if (f.size() != 1) {
			logger.warn("File of id {} does not exist", persistentId);
		}
		return f.get(0);
	}

	@Override
	public void storeFile(IWorkspace workspace, String filename, byte[] fileContent) throws WorkspaceUnsupportedException {
		if (!(workspace instanceof EntityWorkspace)) {
			throw new WorkspaceUnsupportedException("IWorkspace instanced passed is not supported by this IStorageWrapper implementation, only use one implementation at a time");
		}
		EntityWorkspace w = (EntityWorkspace) workspace;

		if (FilenameUtils.getExtension(filename).equals("zip")) {
			this.storeArchive(w, filename, fileContent);
		}
		else {
			this.storeIndividualFile(w, filename, fileContent, null);
		}
	}

	private void storeArchive(EntityWorkspace workspace, String filename, byte[] fileContent) {
		try {
			EntityArchive topArchive = new EntityArchive(filename);
			this.database.storeObject(topArchive);

			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(fileContent));
			ZipEntry zipEntry = zis.getNextEntry();
			EntityArchive curArchive = topArchive;

			while (zipEntry != null) {
				if (zipEntry.isDirectory()) {
					String[] dirs = FilenameUtils.separatorsToUnix(zipEntry.getName()).split("/");
					curArchive = topArchive;
					for (String dir : dirs) {
						EntityArchive nextArch = curArchive.getChildren() == null ? null : curArchive.getChildren().stream().filter(x -> x.getFilename().equals(dir)).findAny().orElse(null);
						if (nextArch == null) {
							nextArch = new EntityArchive(dir, curArchive);
							curArchive.addChild(nextArch);
							this.database.storeObject(nextArch);
						}
						curArchive = nextArch;
					}
				}
				else {
					this.storeIndividualFile(workspace, zipEntry.getName(), IOUtils.toByteArray(zis), curArchive);
				}
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void storeIndividualFile(EntityWorkspace workspace, String filename, byte[] fileContent, EntityArchive archive) {
		EntityFile file = new EntityFile(FilenameUtils.getBaseName(filename), FilenameUtils.getExtension(filename), new Timestamp(System.currentTimeMillis()), archive);
		if (!this.filesystem.storeFile(file, fileContent)) {
			return;
		}

		file.setWorkspace(workspace);
		this.database.storeObject(file);
	}
}
