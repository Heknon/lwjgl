/* 
 * Copyright (c) 2002 Lightweight Java Game Library Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are 
 * met:
 * 
 * * Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'Light Weight Java Game Library' nor the names of 
 *   its contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * Created by IntelliJ IDEA.
 * User: nj
 * Date: 12-08-2002
 * Time: 15:21:23
 * To change template for new interface use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package org.lwjgl.opengl;

public class ARBMultisample {
	public static final int GL_MULTISAMPLE_ARB                                      = 0x809D;
	public static final int GL_SAMPLE_ALPHA_TO_COVERAGE_ARB                         = 0x809E;
	public static final int GL_SAMPLE_ALPHA_TO_ONE_ARB                              = 0x809F;
	public static final int GL_SAMPLE_COVERAGE_ARB                                  = 0x80A0;
	public static final int GL_SAMPLE_BUFFERS_ARB                                   = 0x80A8;
	public static final int GL_SAMPLES_ARB                                          = 0x80A9;
	public static final int GL_SAMPLE_COVERAGE_VALUE_ARB                            = 0x80AA;
	public static final int GL_SAMPLE_COVERAGE_INVERT_ARB                           = 0x80AB;
	public static final int GL_MULTISAMPLE_BIT_ARB                                  = 0x20000000;

	static {
		BufferChecks.putGetMap(GL_MULTISAMPLE_ARB, 1);
		BufferChecks.putGetMap(GL_SAMPLE_ALPHA_TO_COVERAGE_ARB, 1);
		BufferChecks.putGetMap(GL_SAMPLE_ALPHA_TO_ONE_ARB, 1);
		BufferChecks.putGetMap(GL_SAMPLE_COVERAGE_ARB, 1);
		BufferChecks.putGetMap(GL_SAMPLE_BUFFERS_ARB, 1);
		BufferChecks.putGetMap(GL_SAMPLES_ARB, 1);
		BufferChecks.putGetMap(GL_SAMPLE_COVERAGE_VALUE_ARB, 1);
		BufferChecks.putGetMap(GL_SAMPLE_COVERAGE_INVERT_ARB, 1);
	}
	
	public static native void glSampleCoverageARB(float value, boolean invert);
}
