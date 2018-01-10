/**
 * Copyright 2013-2018 Asakusa Framework Team.
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
package com.asakusafw.shafu.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.asakusafw.shafu.internal.core.LogUtil;

/**
 * Utilities for I/O.
 */
public final class IoUtils {

    private static final int UNIX_EXEC_MASK = 0111;

    private static final int UNIX_EXEC_MASK_OWNER = 0100;

    private IoUtils() {
        return;
    }

    /**
     * Creates a new temporary folder.
     * @return the created folder
     * @throws IOException if failed to create a temporary folder
     */
    public static File createTemporaryFolder() throws IOException {
        File file = File.createTempFile("dir", null); //$NON-NLS-1$
        if (file.delete() == false) {
            throw new IOException(MessageFormat.format(
                    Messages.IoUtils_errorFailedToDeleteTemporaryFile,
                    file));
        }
        if (file.mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    Messages.IoUtils_errorFailedToCreateTemporaryDirectory,
                    file));
        }
        return file;
    }

    /**
     * Copies a file or folder.
     * @param src the source file or folder
     * @param dst the target file or folder
     * @throws IOException if the operation was failed
     */
    public static void copy(File src, File dst) throws IOException {
        copy(new NullProgressMonitor(), src, dst);
    }

    /**
     * Copies a file or folder.
     * @param monitor the current progress monitor
     * @param src the source file or folder
     * @param dst the target file or folder
     * @throws IOException if the operation was failed
     */
    public static void copy(IProgressMonitor monitor, File src, File dst) throws IOException {
        IFileSystem fs = EFS.getLocalFileSystem();
        IFileStore from = fs.fromLocalFile(src);
        IFileStore to = fs.fromLocalFile(dst);
        try {
            from.copy(to, EFS.OVERWRITE, monitor);
        } catch (CoreException e) {
            LogUtil.log(e.getStatus());
            throw new IOException(MessageFormat.format(
                    Messages.IoUtils_errorFailedToCopyFile,
                    src, dst));
        }
    }

    /**
     * Moves a file or folder.
     * @param src the source file or folder
     * @param dst the target file or folder
     * @throws IOException if the operation was failed
     */
    public static void move(File src, File dst) throws IOException {
        move(new NullProgressMonitor(), src, dst);
    }

    /**
     * Moves a file or folder.
     * @param monitor the current progress monitor
     * @param src the source file or folder
     * @param dst the target file or folder
     * @throws IOException if the operation was failed
     */
    public static void move(IProgressMonitor monitor, File src, File dst) throws IOException {
        IFileSystem fs = EFS.getLocalFileSystem();
        IFileStore from = fs.fromLocalFile(src);
        IFileStore to = fs.fromLocalFile(dst);
        try {
            from.move(to, EFS.OVERWRITE, monitor);
        } catch (CoreException e) {
            LogUtil.log(e.getStatus());
            throw new IOException(MessageFormat.format(
                    Messages.IoUtils_errorFailedToMoveFile,
                    src, dst));
        }
    }

    /**
     * Deletes a file or folder.
     * If the target file does not exist, this operation has no effects.
     * @param file the target file or folder
     * @throws IOException if the operation was failed
     */
    public static void delete(File file) throws IOException {
        delete(new NullProgressMonitor(), file);
    }

    /**
     * Deletes a file or folder.
     * If the target file does not exist, this operation has no effects.
     * @param monitor the current progress monitor
     * @param file the target file or folder
     * @throws IOException if the operation was failed
     */
    public static void delete(IProgressMonitor monitor, File file) throws IOException {
        boolean result = delete0(monitor, file);
        if (result == false) {
            throw new IOException(MessageFormat.format(
                    Messages.IoUtils_errorFailedToDeleteFile,
                    file));
        }
    }

    /**
     * Deletes a file or folder quietly.
     * If the target file does not exist, this operation has no effects.
     * @param file the target file or folder
     */
    public static void deleteQuietly(File file) {
        deleteQuietly(new NullProgressMonitor(), file);
    }

    /**
     * Deletes a file or folder quietly.
     * @param monitor the current progress monitor
     * If the target file does not exist, this operation has no effects.
     * @param file the target file or folder
     */
    public static void deleteQuietly(IProgressMonitor monitor, File file) {
        delete0(monitor, file);
    }

    private static boolean delete0(IProgressMonitor monitor, File file) {
        if (file.exists() == false) {
            monitor.done();
            return true;
        }
        IFileSystem fs = EFS.getLocalFileSystem();
        IFileStore store = fs.fromLocalFile(file);
        try {
            store.delete(EFS.ATTRIBUTE_SYMLINK, monitor);
            return true;
        } catch (CoreException e) {
            LogUtil.log(e.getStatus());
            return false;
        }
    }

    /**
     * Extracts a {@code *.zip} archive into a new temporary folder.
     * @param monitor the progress monitor
     * @param archiveFile the archive file
     * @return the temporary folder with contents in the archive
     * @throws IOException if failed to extract the archive
     */
    public static File extractZip(IProgressMonitor monitor, File archiveFile) throws IOException {
        File temporary = createTemporaryFolder();
        boolean succeed = false;
        try {
            extractZip(monitor, archiveFile, temporary);
            succeed = true;
            return temporary;
        } finally {
            if (succeed == false) {
                deleteQuietly(temporary);
            }
        }
    }

    /**
     * Extracts a {@code *.tar.gz} archive into a new temporary folder.
     * @param monitor the progress monitor
     * @param archiveFile the archive file
     * @return the temporary folder with contents in the archive
     * @throws IOException if failed to extract the archive
     */
    public static File extractTarGz(IProgressMonitor monitor, File archiveFile) throws IOException {
        File temporary = createTemporaryFolder();
        boolean succeed = false;
        try {
            extractTarGz(monitor, archiveFile, temporary);
            succeed = true;
            return temporary;
        } finally {
            if (succeed == false) {
                deleteQuietly(temporary);
            }
        }
    }

    /**
     * Extracts a {@code *.zip} archive into the target folder.
     * @param monitor the progress monitor
     * @param archiveFile the archive file
     * @param targetDirectory the target folder
     * @throws IOException if failed to extract the archive
     */
    public static void extractZip(
            IProgressMonitor monitor,
            File archiveFile,
            File targetDirectory) throws IOException {
        SubMonitor sub = SubMonitor.convert(monitor, Messages.IoUtils_monitorExtractZip, 10);
        try {
            ZipFile zip = new ZipFile(archiveFile);
            try {
                Enumeration<ZipArchiveEntry> entries = zip.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        createDirectory(targetDirectory, entry);
                    } else {
                        InputStream input = zip.getInputStream(entry);
                        try {
                            File file = createFile(targetDirectory, entry, input);
                            setFileMode(file, entry.getUnixMode());
                        } finally {
                            input.close();
                        }
                        sub.worked(1);
                        sub.setWorkRemaining(10);
                    }
                }
            } finally {
                zip.close();
            }
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }
    }

    /**
     * Extracts a {@code *.tar.gz} archive into the target folder.
     * @param monitor the progress monitor
     * @param archiveFile the archive file
     * @param targetDirectory the target folder
     * @throws IOException if failed to extract the archive
     */
    public static void extractTarGz(
            IProgressMonitor monitor,
            File archiveFile,
            File targetDirectory) throws IOException {
        SubMonitor sub = SubMonitor.convert(monitor, Messages.IoUtils_monitorExtractTarGz, 10);
        try {
            InputStream input = new FileInputStream(archiveFile);
            try {
                ArchiveInputStream archive = new TarArchiveInputStream(new GzipCompressorInputStream(input));
                while (true) {
                    ArchiveEntry entry = archive.getNextEntry();
                    if (entry == null) {
                        break;
                    }
                    if (entry.isDirectory()) {
                        createDirectory(targetDirectory, entry);
                    } else {
                        File file = createFile(targetDirectory, entry, archive);
                        if (entry instanceof TarArchiveEntry) {
                            setFileMode(file, ((TarArchiveEntry) entry).getMode());
                        }
                        sub.worked(1);
                        sub.setWorkRemaining(10);
                    }
                }
            } finally {
                input.close();
            }
        } finally {
            monitor.done();
        }
    }

    private static void setFileMode(File file, int unixMode) {
        if (unixMode <= 0) {
            return;
        }
        if ((unixMode & UNIX_EXEC_MASK) != 0) {
            boolean ownerOnly = (unixMode & UNIX_EXEC_MASK) == UNIX_EXEC_MASK_OWNER;
            file.setExecutable(true, ownerOnly);
        }
    }

    private static void createDirectory(File base, ArchiveEntry entry) throws IOException {
        File file = new File(base, entry.getName());
        if (file.mkdirs() == false && file.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    Messages.IoUtils_errorFailedToCreateDirectory,
                    base));
        }
    }

    private static File createFile(File base, ArchiveEntry entry, InputStream contents) throws IOException {
        File file = new File(base, entry.getName());
        File parent = file.getParentFile();
        parent.mkdirs();
        OutputStream output = new FileOutputStream(file);
        try {
            IOUtils.copy(contents, output);
        } finally {
            output.close();
        }
        return file;
    }
}
