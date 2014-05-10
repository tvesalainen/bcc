/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.bcc;

import java.io.IOException;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;

/**
 *
 * @author Timo Vesalainen
 */
public interface ClassCompiler
{
    /**
     * Set the ProcessingEnvironment
     * @param env ProcessingEnvironment 
     */
    void setProcessingEnvironment(ProcessingEnvironment env);
    /**
     * Compile classfile
     * @throws IOException
     * @throws ReflectiveOperationException 
     */
    void compile() throws IOException, ReflectiveOperationException;
    /**
     * Save compiled classfile to class directory. If source directory was
     * set, byte code source file .jasm is stored in source directory. Additional
     * document files are stored in sor/doc-files.
     * @throws IOException 
     */
    void saveClass() throws IOException;
}
