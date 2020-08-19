/**
 * Copyright (C) 2013-2017 Steffen Schaefer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wisepersist.gradle.plugins.gwt;

import java.io.File;

import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.OutputDirectory;

/**
 * Task to run the GWT compiler for development quality output.
 */
@CacheableTask
public class GwtDraftCompile extends AbstractGwtCompile {
	public GwtDraftCompile() {
		setDraftCompile(true);
	}

	/** {@inheritDoc} */
	@Override
	@OutputDirectory
	public File getWar() {
		return super.getWar();
	}
}