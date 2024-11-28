/*
 * Copyright (c) 2010, 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.common.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.nio.channels.FileChannel;

import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileTools
{
    private static Logger log = LogManager.getLogger(FileTools.class);

    public static final String DIGEST =
        System.getProperty("artifacts.common.file.cmp.digest", "MD5");

    private FileTools() {
    }


    /** Remove everything after dot from name. */
    public static final String removeExtension(String name) {
        int index = name.lastIndexOf('.');
        return index == -1
            ? name
            : name.substring(0, index);
    }


    public static File getDirectory(String path, String name) {
        if (path == null || name == null) {
            return null;
        }

        File dir = new File(path, name);

        if (!dir.exists()) {
            log.debug(
                "Directory '" + dir.getAbsolutePath() + "' doesn't " +
                "exist. Try to create it.");

            return dir.mkdir() ? dir : null;
        }
        else {
            return dir.isDirectory() ? dir : null;
        }
    }

    public static File repair(File file) {
        file = file.getAbsoluteFile();
        if (file.exists()) {
            return file;
        }
        Deque<String> parts = new ArrayDeque<String>();
        File curr = file;
        while (curr != null) {
            String name = curr.getName();
            if (name.length() > 0) {
                parts.push(curr.getName());
            }
            curr = curr.getParentFile();
        }

        curr = null;
        OUTER: while (!parts.isEmpty()) {
            String f = parts.pop();
            log.debug("fixing: '" + f + "'");
            if (f.equals(".") || f.equals("..")) {
                // No need to fix . or ..
                continue;
            }
            if (curr == null) {
                // XXX: Not totaly correct because there
                // more than one root on none unix systems.
                for (File root: File.listRoots()) {
                    File [] files = root.listFiles();
                    if (files == null) {
                        log.warn("cannot list '" + root);
                        continue;
                    }
                    for (File candidate: files) {
                        if (candidate.getName().equalsIgnoreCase(f)) {
                            curr = new File(root, candidate.getName());
                            continue OUTER;
                        }
                    }
                }
                break;
            }
            else {
                File [] files = curr.listFiles();
                if (files == null) {
                    log.warn("cannot list: '" + curr + "'");
                    return file;
                }
                for (File candidate: files) {
                    if (candidate.getName().equalsIgnoreCase(f)) {
                        curr = new File(curr, candidate.getName());
                        continue OUTER;
                    }
                }
                curr = null;
                break;
            }
        }

        if (curr == null) {
            log.warn("cannot repair path '" + file + "'");
            return file;
        }

        return curr;
    }

    /** Object that can calculate hash of file, compare two hashed files etc. */
    public static class HashedFile
    implements Comparable<HashedFile>
    {
        protected File    file;
        protected long    length;
        protected byte [] hash;

        public HashedFile(File file) {
            this.file = file;
            length = file.length();
        }

        public File getFile() {
            return file;
        }

        protected byte [] getHash() {
            if (hash == null) {
                InputStream in = null;

                try {
                    in = new FileInputStream(file);

                    MessageDigest digest = MessageDigest.getInstance(DIGEST);

                    byte [] buf = new byte[40*1024];
                    int r;

                    while ((r = in.read(buf)) >= 0) {
                        digest.update(buf, 0, r);
                    }

                    hash = digest.digest();
                }
                catch (IOException ioe) {
                    log.error(ioe);
                    hash = new byte[0];
                }
                catch (NoSuchAlgorithmException nsae) {
                    log.error(nsae);
                    hash = new byte[0];
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (IOException ioe) {
                            log.error(ioe);
                        }
                    }
                }
            }
            return hash;
        }

        @Override
        public int compareTo(HashedFile other) {
            if (length < other.length) return -1;
            if (length > other.length) return +1;
            return compare(getHash(), other.getHash());
        }

        private static int compare(byte [] a, byte [] b) {
            if (a.length < b.length) return -1;
            if (a.length > b.length) return +1;
            for (int i = 0; i < a.length; ++i) {
                int x = a[i] & 0xff;
                int y = b[i] & 0xff;
                if (x < y) return -1;
                if (x > y) return +1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof HashedFile
                && ((HashedFile)other).compareTo(this) == 0;
        }

        @Override
        public int hashCode() {
            return (int)(length ^ (length >>> 32));
        }
    } // class HashedFile

    public static List<File> uniqueFiles(List<File> files) {

        Set<HashedFile> set = new HashSet<HashedFile>();

        for (File file: files) {
            if (!set.add(new HashedFile(file))) {
                log.warn("file '" + file + "' is a duplicate.");
            }
        }

        ArrayList<File> out = new ArrayList<File>(set.size());
        for (HashedFile hf: set) {
            out.add(hf.file);
        }

        return out;
    }

    public interface FileVisitor {
        boolean visit(File file);
    } // Visitor

    public static void walkTree(File root, FileVisitor visitor) {

        Deque<File> stack = new ArrayDeque<File>();

        stack.push(root);

        while (!stack.isEmpty()) {
            File current = stack.pop();
            if (!visitor.visit(current)) break;
            if (current.isDirectory()) {
                File [] subs = current.listFiles();
                if (subs != null) {
                    for (File f: subs) {
                        stack.push(f);
                    }
                }
            }
        }
    }

    /**
     * Deletes everything in a directory.
     *
     * @param dir The directory.
     */
    public final static void deleteContent(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file: files) {
                deleteRecursive(file);
            }
        }

        return;
    }

    /**
     * Delete <i>file</i> and everything in <i>file</i> if it is a directory.
     *
     * @param file The file or directory.
     * @return true, if deletion was successful - otherwise false.
     */
    public final static boolean deleteRecursive(File file) {

        if (file == null) {
            return false;
        }

        if (file.isDirectory()) {
            File [] files = file.listFiles();
            if (files != null) {
                for (File sub: files) {
                    if (!deleteRecursive(sub)) {
                        return false;
                    }
                }
            }
        }

        return file.delete();
    }
    public static final FileFilter ACCEPT_ALL = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return true;
        }
    };

    public static void createZipArchive(
        File         file,
        OutputStream outputStream
    )
    throws IOException
    {
        createZipArchive(file, outputStream, ACCEPT_ALL);
    }

    /**
     * Put the given file or directory into a zip archive.
     *
     * @param file The file or directory.
     * @param outputStream The stream to write the archive to.
     * @throws IOException if an error occured while zip creation or writing to
     * output stream.
     */
    public static void createZipArchive(
        File         file,
        OutputStream outputStream,
        FileFilter   filter
    )
    throws IOException
    {
        ZipOutputStream out = new ZipOutputStream(outputStream);

        if (filter.accept(file) && file.isFile()) {
            copyFileToZip("", file, out);
        }
        else if (file.isDirectory()) {

            Deque<PrefixDir> stack = new ArrayDeque<PrefixDir>();
            stack.push(new PrefixDir(file.getName() + "/", file));

            while (!stack.isEmpty()) {
                PrefixDir pd = stack.pop();

                ZipEntry dirEntry = new ZipEntry(pd.prefix);
                out.putNextEntry(dirEntry);
                out.closeEntry();

                File [] files = pd.dir.listFiles(filter);
                if (files != null) {
                    for (File sub: files) {
                        if (sub.isDirectory()) {
                            stack.push(new PrefixDir(
                                pd.prefix + sub.getName() + "/",
                                sub));
                        }
                        else if (sub.isFile()) {
                            copyFileToZip(pd.prefix, sub, out);
                        }
                    }
                }
            }
        }

        out.finish();
    }


    public static void extractArchive(File archive, File destDir)
    throws IOException {
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        ZipFile zipFile = new ZipFile(archive);
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            byte [] buffer = new byte[16384];

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                String entryFileName = entry.getName();

                File dir = buildDirectoryHierarchyFor(entryFileName, destDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                if (!entry.isDirectory()) {
                    BufferedInputStream bis = new BufferedInputStream(
                        zipFile.getInputStream(entry));
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(
                            new FileOutputStream(new File(destDir, entryFileName)));

                        try {
                            int len;
                            while ((len = bis.read(buffer)) > 0) {
                                bos.write(buffer, 0, len);
                            }
                            bos.flush();
                        }
                        finally {
                            bos.close();
                        }
                    }
                    finally {
                        bis.close();
                    }
                } // is file
            }
        }
        finally {
            zipFile.close();
        }
    }

    private static File buildDirectoryHierarchyFor(
        String entryName,
        File destDir)
    {
        int lastIndex = entryName.lastIndexOf('/');
        String internalPathToEntry = entryName.substring(0, lastIndex + 1);
        return new File(destDir, internalPathToEntry);
    }

    /**
     * A class representing a directory with a prefix.
     */
    private static final class PrefixDir {

        String prefix;
        File   dir;

        public PrefixDir(String prefix, File dir) {
            this.prefix = prefix;
            this.dir    = dir;
        }

    } // class PrefixDir

    /**
     * Write a file to zip archive.
     *
     * @param prefix A prefix.
     * @param file The file.
     * @param out The output stream.
     * @throws IOException if an error occured while writing to zip output
     * stream.
     */
    private static void copyFileToZip(
        String          prefix,
        File            file,
        ZipOutputStream out
    )
    throws IOException
    {
        String   entryName = prefix + file.getName();
        ZipEntry entry     = new ZipEntry(entryName);
        out.putNextEntry(entry);
        InputStream in = null;
        try {
            in =
                new BufferedInputStream(
                new FileInputStream(file), 20*1024);

            byte [] buf = new byte[2048];

            int r;
            while ((r = in.read(buf)) > 0) {
                out.write(buf, 0, r);
            }
        }
        finally {
            if (in != null) {
                try { in.close(); }
                catch (IOException ioe) {}
            }
        }
        out.closeEntry();
    }


    /**
     * Copies a <i>src</i> file to <i>target</i>.
     *
     * @param src A file (not a directory) that should be copied.
     * @param target The destination. This might be a file or a directory.
     *
     * @return true, if <i>src</i> has been successfully copied; otherwise
     * false.
     */
    public static boolean copyFile(File src, File target)
    throws IOException
    {
        if (src == null || !src.exists()) {
            log.warn("Source file does not exist!");
            return false;
        }

        if (!src.canRead()) {
            log.warn("Cannot read Source file!");
            return false;
        }

        if (src.isDirectory()) {
            log.warn("Source is a directory!");
            return false;
        }

        if (target.isDirectory()) {
            target = new File(target, src.getName());
        }

        FileInputStream  in  = null;
        FileOutputStream out = null;

        try {
            in  = new FileInputStream(src);
            out = new FileOutputStream(target);

            FileChannel inChannel  = in.getChannel();
            FileChannel outChannel = out.getChannel();

            inChannel.transferTo(0l, inChannel.size(), outChannel);

            return true;
        }
        catch (IOException ioe) {
            log.warn(ioe, ioe);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ioe) { /* do nothing here */ }
            }

            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ioe) { /* do nothing here */ }
            }
        }

        return false;
    }


    /**
     * Copies a directory <i>source</i> to a destination path <i>dest</i>.
     *
     * @param source A directory that should be copied.
     * @param dest A destination directory which is created if it is not
     * existing yet.
     *
     * @return true, if the directory has been successfully copied; otherwise
     * false.
     */
    public static boolean copyDirectory(final File source, final File dest) {
        if (source == null || !source.exists()) {
            log.warn("Source directory does not exist!");
            return false;
        }

        if (!source.isDirectory()) {
            log.warn("Source is not a directory!");
            return false;
        }

        if (dest == null) {
            log.warn("Destination directory is null!");
            return false;
        }

        if (!dest.exists()) {
            if (!dest.mkdir()) {
                log.warn("Cannot create destination directory!");
                return false;
            }
        }

        File[] children = source.listFiles();
        int    failed   = 0;

        if (children != null && children.length > 0) {
            for (File child: children) {
                if (child.isFile()) {
                    try {
                        if (!copyFile(child, dest)) {
                            failed++;
                        }
                    }
                    catch (IOException ioe) {
                        log.warn(ioe, ioe);
                        failed++;
                    }
                }
                else if (child.isDirectory()) {
                    copyDirectory(child, new File(dest, child.getName()));
                }
            }
        }

        log.debug("Failed to copy " + failed + " files.");

        return true;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
