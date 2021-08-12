package desktop.starter.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import desktop.starter.generator.model.AppConfigModel;
import desktop.starter.generator.model.JVMConfig;
import desktop.starter.generator.util.Util;
import desktop.starter.model.AppConfig;
import desktop.starter.util.DesktopUtil;
import desktop.starter.util.OSInfo;
import desktop.starter.util.OSInfo.Arch;
import desktop.starter.util.OSInfo.OSType;
import desktop.starter.util.model.download.Metadata;
import desktop.starter.util.model.download.Repo;
import desktop.starter.generator.service.FileMapperService;

public class AppConfigCreator {
	public static final String APP_CONFIG_GENERATOR = "appConfigModel.json";
	public static final String DOMAIN_CONFIG = "domainConfig.json";
	public static final String TEMP_APP_CONFIG = "tempAppConfig.json";
	public static final String TARGET_OUT_FOLDER = "target/out";
	public static final String APP_DEPENDENCISES_CONFIG = "dependencises.json";
	public static final String APP_RESOURCES_CONFIG = "resources.json";
	public static final String JAVA_CONFIG = "javaConfig.json";
	FileMapperService fms;
	/**
	 * @param configFile contains config app
	 * @param fms
	 * @return generated AppConfig
	 * @throws NoSuchAlgorithmException
	 */
	public AppConfigCreator() {
		fms = new FileMapperService();
	}
	
	public AppConfig createConfig(AppConfigModel configFile, List<Domain> domains)
			throws IOException, NoSuchAlgorithmException {
		AppConfig appConfig = new AppConfig();
		String version = Paths.get(configFile.getAppName(), String.valueOf(configFile.getAppVersion())).toString();	
		appConfig.setAppName(configFile.getAppName());
		appConfig.setAppVersion(configFile.getAppVersion());
		appConfig.setArguments(configFile.getArguments());
		appConfig.setMainClass(configFile.getMainClass());
		fms.copyFile(Paths.get(configFile.getAppFolder()), Paths.get(TARGET_OUT_FOLDER, version));
		appConfig.setAppDependencies(createRepo(Paths.get(configFile.getAppDependencies()),
				Paths.get(configFile.getAppDependencies()), domains,
				Paths.get(version, Paths.get(configFile.getAppDependencies()).getFileName().toString()).toString()));
		fms.write(appConfig.getAppDependencies(), Paths.get(TARGET_OUT_FOLDER, version, APP_DEPENDENCISES_CONFIG));
		appConfig.setAppDependencies(createRepo(Paths.get(TARGET_OUT_FOLDER, version),
				Paths.get(TARGET_OUT_FOLDER, version, APP_DEPENDENCISES_CONFIG), domains, version));
		appConfig.setAppResources(createRepo(Paths.get(configFile.getAppResources()),
				Paths.get(configFile.getAppResources()), domains,
				Paths.get(version, Paths.get(configFile.getAppResources()).getFileName().toString()).toString()));
		fms.write(appConfig.getAppResources(), Paths.get(TARGET_OUT_FOLDER, version, APP_RESOURCES_CONFIG));
		appConfig.setAppResources(createRepo(Paths.get(TARGET_OUT_FOLDER, version),
				Paths.get(TARGET_OUT_FOLDER, version, APP_RESOURCES_CONFIG), domains, version));
		Object app = fms.read(Paths.get(configFile.getJavaConfig(), TEMP_APP_CONFIG), AppConfig.class);
		appConfig.setAppFileRepo(createRepo(Paths.get(configFile.getAppFolder()),
				Paths.get(configFile.getAppFolder(), configFile.getAppFile()), domains,
				Paths.get(configFile.getAppName()).toString()));
		appConfig.setJavaRepo(((AppConfig) app).getJavaRepo());
		if (configFile.isGeneretedJava()) {
			createJreConfig(domains, configFile);
			appConfig.setJavaRepo(createRepo(Paths.get(TARGET_OUT_FOLDER), Paths.get(TARGET_OUT_FOLDER, JAVA_CONFIG),
					domains, Paths.get(configFile.getAppName()).toString()));
		}
		return appConfig;
	}

	public void saveFiles(AppConfig config) {
		// todo test before add exists or not(file will have filename.zip.signature
		// check current remote file withsignature can be like this
		// {"length":1,"sha1":"ddf"};
		// and if file exists we can add filename.zip.1 ... 10 etc and generage
		// signature filename.zip.signature

	}

	/**
	 * we can save config on remote machines like files
	 *
	 * @param config
	 */
	public void printConfig(AppConfig config) {
		// saved config we ca
	}

	/*
	 * protected FTPClient create(FtpInfo info) throws IOException { FTPClient
	 * ftpClient = new FTPClient(); ftpClient.setControlKeepAliveTimeout(300);
	 * ftpClient.connect(info.getAddress(), port); ftpClient.login(info.getLogin(),
	 * info.getPass()); logger.debug("ftp reply code  is " +
	 * ftpClient.getReplyCode()); ftpClient.enterLocalPassiveMode();
	 * ftpClient.setFileType(FTP.BINARY_FILE_TYPE); return ftpClient; }
	 *
	 * public void deleteFile(String path) throws IOException { synchronized (this)
	 * { if (!init) init(); for (FTPClient client : ftpClients) { if
	 * (!client.deleteFile(path)) { throw new
	 * RuntimeException("problem with save of the file: " + path); } } } }
	 *
	 * private void ftpCreateDirectoryTree(FTPClient client, String dirTree) throws
	 * IOException {
	 *
	 * boolean dirExists = true;
	 *
	 * // tokenize the string and attempt to change into each directory level. If
	 * you // cannot, then start creating. String[] directories =
	 * dirTree.substring(0, dirTree.lastIndexOf("/")).split("/"); for (String dir :
	 * directories) { if (!dir.isEmpty()) { if (dirExists) {
	 * logger.trace("change working directory and check " + dir); dirExists =
	 * client.changeWorkingDirectory(dir); } if (!dirExists) {
	 * logger.debug("make working directory" + dir); if (!client.makeDirectory(dir))
	 * { throw new IOException("Unable to create remote directory '" + dir +
	 * "'.  error='" + client.getReplyString() + "'"); } if
	 * (!client.changeWorkingDirectory(dir)) {
	 * logger.debug("change working directory " + dir); throw new
	 * IOException("Unable to change into newly created remote directory '" + dir +
	 * "'.  error='" + client.getReplyString() + "'"); } } } }
	 * client.changeWorkingDirectory("/"); }
	 *
	 * @PreDestroy public void close() { for (FTPClient client : ftpClients) { try {
	 * if (client.isConnected()) { client.logout(); } } catch (IOException ex) {
	 * logger.debug("", ex); } finally { try { client.disconnect(); } catch
	 * (IOException e) { logger.error("", e); } } } }
	 *
	 * public void storeFile(byte[] array, String path) throws IOException { String
	 * decode = UriUtils.decode(path, StandardCharsets.ISO_8859_1.toString());
	 * synchronized (this) { if (!init) init(); try { for (FTPClient client :
	 * ftpClients) { logger.debug("trying to save file " + path + " on server " +
	 * client.getRemoteAddress()); ftpCreateDirectoryTree(client, decode); if
	 * (!client.storeFile(decode, new ByteArrayInputStream(array))) {
	 * cleanMetadata(decode); throw new RuntimeException(client.getReplyCode() + " "
	 * + client.getReplyString() + " " + decode); } logger.debug("saved file " +
	 * path + " on server " + client.getRemoteAddress()); }
	 *
	 * } catch (FTPConnectionClosedException f) { logger.warn("", f); try {
	 * Thread.sleep(10 * 1000L); } catch (InterruptedException e) { } close();
	 * init(); storeFile(array, path); } } }
	 *
	 * protected void init() throws IOException { init = true; Resource resource =
	 * new ClassPathResource(defaultConfigFile); String text =
	 * IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
	 * List<FtpInfo> servers = ParserUtil.createMapper().readValue(text, new
	 * TypeReference<List<FtpInfo>>() { }); ftpClients = new ArrayList<>(); for
	 * (FtpInfo s : servers) { ftpClients.add(create(s)); } }
	 */

	private Repo createRepo(Path jvms, Path folder, List<Domain> domains, String str) throws IOException {
		List<String> domain = domains.stream().map(Domain::getDomain).collect(Collectors.toList());
		List<Metadata> metadataList = Files.walk(folder).filter(Files::isRegularFile).map(Util.wrap(e -> {
			Path s = jvms.relativize(e);
			Metadata m = new Metadata();
			m.setSha1(DesktopUtil.getChecksum(e.toFile(), "SHA-1"));
			m.setPath(s.toString());
			m.setSize(e.toFile().length());
			if (Objects.nonNull(str)) {
				s = Paths.get(str, s.toString());
			}
			m.setRelativeUrl(s.toString());
			return m;
		})).collect(Collectors.toList());
		Repo r = new Repo();
		r.setResources(metadataList);
		r.setRepositories(domain);
		return r;
	}

	private List<Path> listPath(Path p) throws IOException {
		return Files.walk(p, 1).filter(entry -> !entry.equals(p)).collect(Collectors.toList());
	}

	private void createJreConfig(List<Domain> domains, AppConfigModel configFile)
			throws IOException, NoSuchAlgorithmException {
		JVMConfig jvm = new JVMConfig();
		jvm.setJvms(new HashMap<OSInfo.OSType, Map<Arch, Map<String, Repo>>>());
		for (Path pathTypeOS : listPath(Paths.get(configFile.getJavaFolder()))) {
			OSType type = OSType.valueOf(pathTypeOS.getFileName().toString().toUpperCase(Locale.ROOT));
			jvm.getJvms().put(type, new HashMap<OSInfo.Arch, Map<String, Repo>>());
			for (Path pathArch : listPath(pathTypeOS)) {
				Arch arch = Arch.valueOf(pathArch.getFileName().toString().toLowerCase(Locale.ROOT));
				jvm.getJvms().get(type).put(arch, new HashMap<String, Repo>());
				for (Path pathKey : listPath(pathArch)) {
					String key = String.valueOf(pathKey.getFileName().toString().toLowerCase(Locale.ROOT));
					Repo repo = new Repo();
					jvm.getJvms().get(type).get(arch).put(key, repo);
					for (Path pathJre : listPath(pathKey)) {
						// Create json from all jvm
						Repo createdJson = createRepo(pathKey.getParent(), pathJre, domains,
								Paths.get(configFile.getAppName()).toString());
						Path jvmConfig = Paths.get("jvms", type.toString().toLowerCase(Locale.ROOT), arch.toString(),
								key, String.valueOf(pathJre.getFileName().toString()) + ".json");
						fms.write(createdJson, jvmConfig);
						repo.setResources(Arrays.asList(Metadata.getResource(jvmConfig)));
						repo.setRepositories(domains.stream().map(e -> e.getDomain()).collect(Collectors.toList()));
					}
				}
			}
		}
		// Create json Config from all json
		fms.write(jvm, Paths.get(TARGET_OUT_FOLDER, JAVA_CONFIG));
	}	
}