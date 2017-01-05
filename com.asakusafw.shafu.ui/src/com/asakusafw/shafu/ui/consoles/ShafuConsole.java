/**
 * Copyright 2013-2017 Asakusa Framework Team.
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
package com.asakusafw.shafu.ui.consoles;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.asakusafw.shafu.core.gradle.GradleContext;
import com.asakusafw.shafu.internal.ui.Activator;
import com.asakusafw.shafu.internal.ui.LogUtil;

/**
 * Console for this plugin.
 * @since 0.1.0
 * @version 0.3.3
 */
public final class ShafuConsole extends MessageConsole {

    /**
     * The console name.
     */
    public static final String NAME = Messages.ShafuConsole_name;

    private final Color backgroundColor;

    private final Color outputColor;

    private final Color errorColor;

    private final Color infoColor;

    private final ShafuOutputStream outputStream;

    private final ShafuOutputStream errorStream;

    private final ShafuOutputStream infoStream;

    /**
     * Creates a new instance.
     */
    public ShafuConsole() {
        super(NAME, null);
        this.backgroundColor = createColor(255, 255, 255);
        this.outputColor = createColor(0, 0, 0);
        this.errorColor = createColor(255, 0, 0);
        this.infoColor = createColor(0, 0, 255);

        this.setBackground(backgroundColor);
        this.outputStream = new ShafuOutputStream(this, Charset.defaultCharset());
        this.outputStream.setColor(outputColor);
        this.errorStream = new ShafuOutputStream(this, Charset.defaultCharset());
        this.errorStream.setColor(errorColor);
        this.infoStream = new ShafuOutputStream(this, Charset.defaultCharset());
        this.infoStream.setColor(infoColor);
    }

    /**
     * Attaches this console to the target {@link GradleContext}.
     * @param context the target context
     */
    public void attachTo(GradleContext context) {
        context.withStandardInput(getStandardInputStream());
        context.withStandardOutput(getStandardOutputStream());
        context.withStandardError(getStandardErrorStream());
        context.withInformationOutput(infoStream.toPrintWriter());
    }

    /**
     * Returns the standard input stream for this console.
     * @return the standard input stream
     */
    public InputStream getStandardInputStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
    }

    /**
     * Returns the standard output stream for this console.
     * @return the standard output stream
     */
    public MessageConsoleStream getStandardOutputStream() {
        return outputStream;
    }

    /**
     * Returns the standard error output stream for this console.
     * @return the standard error output stream
     */
    public MessageConsoleStream getStandardErrorStream() {
        return errorStream;
    }

    private Color createColor(int r, int g, int b) {
        return new Color(Activator.getDisplay(), r, g, b);
    }

    @Override
    protected void dispose() {
        try {
            dispose0();
        } finally {
            super.dispose();
        }
    }

    private void dispose0() {
        closeQuietly(outputStream);
        closeQuietly(errorStream);
        backgroundColor.dispose();
        outputColor.dispose();
        errorColor.dispose();
    }

    private static void closeQuietly(Closeable object) {
        try {
            object.close();
        } catch (IOException e) {
            LogUtil.log(IStatus.WARNING, Messages.ShafuConsole_errorFailedToCloseStream, e);
        }
    }

    private static final class ShafuOutputStream extends MessageConsoleStream {

        private final Charset encoding;

        public ShafuOutputStream(ShafuConsole console, Charset encoding) {
            super(console);
            this.encoding = encoding;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            write(new String(b, off, len, encoding));
        }

        @Override
        public void setEncoding(String encoding) {
            throw new UnsupportedOperationException();
        }

        PrintWriter toPrintWriter() {
            return new PrintWriter(new OutputStreamWriter(this, encoding), true);
        }
    }
}
