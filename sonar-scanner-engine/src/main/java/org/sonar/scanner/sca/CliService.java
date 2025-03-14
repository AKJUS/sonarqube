/*
 * SonarQube
 * Copyright (C) 2009-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.scanner.sca;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.api.platform.Server;
import org.sonar.api.utils.System2;
import org.sonar.core.util.ProcessWrapperFactory;
import org.sonar.scanner.config.DefaultConfiguration;
import org.sonar.scanner.repository.TelemetryCache;

/**
 * The CliService class is meant to serve as the main entrypoint for any commands
 * that should be executed by the CLI. It will handle manages the external process,
 * raising any errors that happen while running a command, and passing back the
 * data generated by the command to the caller.
 */
public class CliService {
  private static final Logger LOG = LoggerFactory.getLogger(CliService.class);
  private final ProcessWrapperFactory processWrapperFactory;
  private final TelemetryCache telemetryCache;
  private final System2 system2;
  private final Server server;

  public CliService(ProcessWrapperFactory processWrapperFactory, TelemetryCache telemetryCache, System2 system2, Server server) {
    this.processWrapperFactory = processWrapperFactory;
    this.telemetryCache = telemetryCache;
    this.system2 = system2;
    this.server = server;
  }

  public File generateManifestsZip(DefaultInputModule module, File cliExecutable, DefaultConfiguration configuration) throws IOException, IllegalStateException {
    long startTime = system2.now();
    boolean success = false;
    try {
      var debugLevel = Level.DEBUG;

      String zipName = "dependency-files.zip";
      Path zipPath = module.getWorkDir().resolve(zipName);
      List<String> args = new ArrayList<>();
      args.add(cliExecutable.getAbsolutePath());
      args.add("projects");
      args.add("save-lockfiles");
      args.add("--zip");
      args.add("--zip-filename");
      args.add(zipPath.toAbsolutePath().toString());
      args.add("--directory");
      args.add(module.getBaseDir().toString());

      boolean scaDebug = configuration.getBoolean("sonar.sca.debug").orElse(false);
      if (LOG.isDebugEnabled() || scaDebug) {
        LOG.info("Setting CLI to debug mode");
        args.add("--debug");
        if (scaDebug) {
          // output --debug logs from stderr to the info level logger
          debugLevel = Level.INFO;
        }
      }

      LOG.atLevel(debugLevel).log("Calling ProcessBuilder with args: {}", args);

      Map<String, String> envProperties = new HashMap<>();
      // sending this will tell the CLI to skip checking for the latest available version on startup
      envProperties.put("TIDELIFT_SKIP_UPDATE_CHECK", "1");
      envProperties.put("TIDELIFT_ALLOW_MANIFEST_FAILURES", "1");
      envProperties.put("TIDELIFT_CLI_INSIDE_SCANNER_ENGINE", "1");
      envProperties.put("TIDELIFT_CLI_SQ_SERVER_VERSION", server.getVersion());
      envProperties.putAll(ScaProperties.buildFromScannerProperties(configuration));

      LOG.atLevel(debugLevel).log("Environment properties: {}", envProperties);

      Consumer<String> logConsumer = LOG.atLevel(debugLevel)::log;
      processWrapperFactory.create(module.getWorkDir(), logConsumer, logConsumer, envProperties, args.toArray(new String[0])).execute();
      LOG.info("Generated manifests zip file: {}", zipName);
      success = true;
      return zipPath.toFile();
    } finally {
      telemetryCache.put("scanner.sca.execution.cli.duration", String.valueOf(system2.now() - startTime));
      telemetryCache.put("scanner.sca.execution.cli.success", String.valueOf(success));
    }
  }
}
