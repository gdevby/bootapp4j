package by.gdev.http.upload.download.downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import by.gdev.http.download.model.Headers;
import by.gdev.util.DesktopUtil;
import by.gdev.util.model.download.Metadata;
import by.gdev.util.model.download.Repo;
import by.gdev.utils.service.FileMapperService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class DownloaderJavaContainer extends DownloaderContainer {

	private FileMapperService fileMapperService;
	private String workDir;
	private String jreConfig;

	public void filterNotExistResoursesAndSetRepo(Repo repo, String workDirectory)
			throws NoSuchAlgorithmException, IOException {
		this.repo = new Repo();

		this.repo.setRepositories(repo.getRepositories());
		this.repo.setResources(Lists.newArrayList());
		for (Metadata meta : repo.getResources()) {
			File localFile = Paths.get(workDirectory, meta.getPath()).toAbsolutePath().toFile();
			if (localFile.exists()) {
				String shaLocalFile = DesktopUtil.getChecksum(localFile, Headers.SHA1.getValue());
				BasicFileAttributes attr = Files.readAttributes(localFile.toPath(), BasicFileAttributes.class,
						LinkOption.NOFOLLOW_LINKS);
				if (!attr.isSymbolicLink() & !shaLocalFile.equals(meta.getSha1())) {
					this.repo.getResources().add(meta);
					log.warn("The hash sum of the file is not equal. File " + localFile + " will be deleted. Size = "
							+ localFile.length() / 1024 / 1024);
					FileUtils.deleteDirectory(Paths.get(workDir, "jre_default").toFile());
				} else
					validateJre(repo);
			} else {
				removeJava(localFile.toPath());
				this.repo.getResources().add(meta);
			}
		}
	}

	private void validateJre(Repo repo) throws IOException, NoSuchAlgorithmException {
		if (!Files.exists(Paths.get(workDir, "jre_default")))
			return;
		List<Metadata> configMetadata = fileMapperService.read("jre_default/" + jreConfig, Repo.class).getResources();
		List<Metadata> localeJreMetadata = DesktopUtil.generateMetadataForJre(workDir, "jre_default");
		configMetadata.removeAll(localeJreMetadata);
		if (configMetadata.size() != 0) {
			Metadata m = repo.getResources().get(0);
			removeJava(Paths.get(workDir, m.getPath()));
			this.repo = repo;
			log.warn("problem with jre");
		}
	}

	private void removeJava(Path path) throws IOException {
		FileUtils.deleteDirectory(Paths.get(workDir, "jre_default").toFile());
		FileUtils.deleteQuietly(path.toAbsolutePath().toFile());
	}

	public void containerAllSize(Repo repo) {
		containerSize = repo.getResources().stream().map(Metadata::getSize).reduce(Long::sum).orElse(0L);
	}

	public void downloadSize(Repo repo, String workDirectory) {
		readyDownloadSize = repo.getResources().stream().map(e -> {
			return Paths.get(workDirectory, e.getPath()).toFile().length();
		}).reduce(Long::sum).orElse(0L);
	}
}