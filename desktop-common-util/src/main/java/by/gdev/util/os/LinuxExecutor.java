package by.gdev.util.os;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import by.gdev.util.model.GPUDescription;
import by.gdev.util.model.GPUDriverVersion;
import by.gdev.util.model.GPUsDescriptionDTO;


public class LinuxExecutor implements OSExecutor {

	private static final Path CUDA_VERSION_PATH = Paths.get("/usr/local/cuda/version.txt");

	@Override
	public String execute(String command, int seconds) throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor(seconds, TimeUnit.SECONDS);
		String res = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
		p.getInputStream().close();
		return res;
	}

	@Override
	public GPUsDescriptionDTO getGPUInfo() throws IOException, InterruptedException {
		String res = execute("lshw -C display", 60);
		return getGPUInfo1(res, "product:");
	}

	@Override
	public GPUDriverVersion getGPUDriverVersion() throws IOException {
		if (Files.notExists(CUDA_VERSION_PATH))
			return null;
		String s = new String(Files.readAllBytes(CUDA_VERSION_PATH));
		String[] res = s.split(" ");
		if (res.length == 3) {
			Optional<GPUDriverVersion> op = Arrays.stream(GPUDriverVersion.values())
					.filter(f -> res[2].startsWith(f.getValue())).findFirst();
			if (op.isPresent()) {
				return op.get();
			}
		}
		return null;
	}

	protected GPUsDescriptionDTO getGPUInfo1(String res, String stringStart) {
		String[] params = res.split(System.lineSeparator());
		List<GPUDescription> gpus = Arrays.stream(params).map(String::toLowerCase).filter(e -> e.contains(stringStart))
				.map(s -> {
					GPUDescription g = new GPUDescription();
					g.setName(s.split(":")[1]);
					return g;
				}).collect(Collectors.toList());
		GPUsDescriptionDTO gpusDescriptionDTO = new GPUsDescriptionDTO();
		gpusDescriptionDTO.setRawDescription(res);
		gpusDescriptionDTO.setGpus(gpus);
		return gpusDescriptionDTO;
	}
}