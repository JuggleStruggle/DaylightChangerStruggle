// Directly taken from C2ME's 26.2's source code on GitHub and slightly adjusted to better
// match DaylightChangerStruggle's format as it is required to maintain Yarn mapping. Source:
// https://raw.githubusercontent.com/RelativityMC/C2ME-fabric/refs/heads/dev/26.2.0/buildSrc/src/main/java/com/ishland/c2me/gradle/MavenIntermediaryProvider.java

package com.ishland.c2me.gradle;

import net.fabricmc.loom.configuration.providers.mappings.IntermediaryMappingsProvider;
import net.fabricmc.loom.configuration.providers.mappings.MappingConfiguration;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.provider.Property;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class MavenIntermediaryProvider extends IntermediaryMappingsProvider
{
	public abstract Property<String> getIntermediaryNotation();

	@Override
	public void provide(Path tinyMappings, @Nullable Project project) throws IOException
	{
		if (Files.exists(tinyMappings) && !getRefreshDeps().get())
			return;

		if (project == null) {
			super.provide(tinyMappings, null); return;
		}

		// Download and extract intermediary
		final Path intermediaryJarPath = Files.createTempFile(getName(), ".jar");
		final String intermediaryNotation = this.getIntermediaryNotation().get();

		final ModuleDependency intermediaryDep = this.getDependencyFactory().create(intermediaryNotation);
		final Configuration config = project.getConfigurations().detachedConfiguration(intermediaryDep);

		Files.copy(config.getSingleFile().toPath(), intermediaryJarPath, StandardCopyOption.REPLACE_EXISTING);
		Files.deleteIfExists(tinyMappings);

		MappingConfiguration.extractMappings(intermediaryJarPath, tinyMappings);
	}

}
