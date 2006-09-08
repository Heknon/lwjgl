/*
 * Copyright (c) 2002-2004 LWJGL Project
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
 * * Neither the name of 'LWJGL' nor the names of
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
package org.lwjgl.opengl;

/**
 * This is the Display implementation interface. Display delegates
 * to implementors of this interface. There is one DisplayImplementation
 * for each supported platform.
 * @author elias_naur
 */

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Cursor;

final class WindowsDisplay implements DisplayImplementation {
	private final static int GAMMA_LENGTH = 256;
	private final static int WM_MOUSEMOVE                     = 0x0200;
	private final static int WM_LBUTTONDOWN                   = 0x0201;
	private final static int WM_LBUTTONUP                     = 0x0202;
	private final static int WM_LBUTTONDBLCLK                 = 0x0203;
	private final static int WM_RBUTTONDOWN                   = 0x0204;
	private final static int WM_RBUTTONUP                     = 0x0205;
	private final static int WM_RBUTTONDBLCLK                 = 0x0206;
	private final static int WM_MBUTTONDOWN                   = 0x0207;
	private final static int WM_MBUTTONUP                     = 0x0208;
	private final static int WM_MBUTTONDBLCLK                 = 0x0209;
	private final static int WM_MOUSEWHEEL                    = 0x020A;

	private final static int WM_QUIT						  = 0x0012;
	private final static int WM_SYSCOMMAND					  = 0x0112;
	private final static int WM_PAINT 						  = 0x000F;

	private final static int SC_SIZE          = 0xF000;
	private final static int SC_MOVE          = 0xF010;
	private final static int SC_MINIMIZE      = 0xF020;
	private final static int SC_MAXIMIZE      = 0xF030;
	private final static int SC_NEXTWINDOW    = 0xF040;
	private final static int SC_PREVWINDOW    = 0xF050;
	private final static int SC_CLOSE         = 0xF060;
	private final static int SC_VSCROLL       = 0xF070;
	private final static int SC_HSCROLL       = 0xF080;
	private final static int SC_MOUSEMENU     = 0xF090;
	private final static int SC_KEYMENU       = 0xF100;
	private final static int SC_ARRANGE       = 0xF110;
	private final static int SC_RESTORE       = 0xF120;
	private final static int SC_TASKLIST      = 0xF130;
	private final static int SC_SCREENSAVE    = 0xF140;
	private final static int SC_HOTKEY        = 0xF150;
	private final static int SC_DEFAULT       = 0xF160;
	private final static int SC_MONITORPOWER  = 0xF170;
	private final static int SC_CONTEXTHELP   = 0xF180;
	private final static int SC_SEPARATOR     = 0xF00F;

	private final static int SM_CXCURSOR      = 13;

	private final static int SIZE_RESTORED        = 0;
	private final static int SIZE_MINIMIZED       = 1;
	private final static int SIZE_MAXIMIZED       = 2;
	private final static int WM_SIZE          = 0x0005;
	private final static int WM_ACTIVATE          = 0x0006;
	private final static int     WA_INACTIVE      = 0;
	private final static int     WA_ACTIVE        = 1;
	private final static int     WA_CLICKACTIVE   = 2;
	private final static int SW_SHOWMINNOACTIVE   = 7;
	private final static int SW_RESTORE           = 9;

	private static WindowsDisplay current_display;

	private WindowsDisplayPeerInfo peer_info;

	private WindowsKeyboard keyboard;
	private WindowsMouse mouse;

	private boolean close_requested;
	private boolean is_dirty;

	private ByteBuffer current_gamma;
	private ByteBuffer saved_gamma;
	private DisplayMode current_mode;

	private boolean mode_set;
	private boolean isFullscreen;
	private boolean isMinimized;
	private boolean isFocused;
	private boolean did_maximize;
	private boolean inAppActivate;

	public WindowsDisplay() {
		current_display = this;
	}

	public void createWindow(DisplayMode mode, boolean fullscreen, int x, int y) throws LWJGLException {
		close_requested = false;
		is_dirty = false;
		isFullscreen = fullscreen;
		isMinimized = false;
		isFocused = false;
		did_maximize = false;
		nCreateWindow(mode, fullscreen, x, y);
		peer_info.initDC();
	}
	private native void nCreateWindow(DisplayMode mode, boolean fullscreen, int x, int y) throws LWJGLException;

	public void destroyWindow() {
		nDestroyWindow();
		if (isFullscreen)
			resetCursorClipping();
	}
	private static native void nDestroyWindow();
	private static native void resetCursorClipping();
	private static native void setupCursorClipping(long hwnd) throws LWJGLException;

	public void switchDisplayMode(DisplayMode mode) throws LWJGLException {
		nSwitchDisplayMode(mode);
		current_mode = mode;
		mode_set = true;
	}
	private static native void nSwitchDisplayMode(DisplayMode mode) throws LWJGLException;

	/*
	 * Called when the application is alt-tabbed to or from
	 */
	private void appActivate(boolean active) {
		if (inAppActivate) {
			return;
		}
		inAppActivate = true;
		isFocused = active;
		if (active) {
			if (isFullscreen) {
				restoreDisplayMode();
			}
			showWindow(getHwnd(), SW_RESTORE);
			setForegroundWindow(getHwnd());
			setFocus(getHwnd());
			did_maximize = true;
		} else if (isFullscreen) {
			showWindow(getHwnd(), SW_SHOWMINNOACTIVE);
			resetDisplayMode();
		}
		inAppActivate = false;
	}
	private static native void showWindow(long hwnd, int mode);
	private static native void setForegroundWindow(long hwnd);
	private static native void setFocus(long hwnd);

	private void restoreDisplayMode() {
		try {
			doSetGammaRamp(current_gamma);
		} catch (LWJGLException e) {
			LWJGLUtil.log("Failed to restore gamma: " + e.getMessage());
		}

		if (!mode_set) {
			mode_set = true;
			try {
				nSwitchDisplayMode(current_mode);
			} catch (LWJGLException e) {
				LWJGLUtil.log("Failed to restore display mode: " + e.getMessage());
			}
		}
	}

	public void resetDisplayMode() {
		try {
			doSetGammaRamp(saved_gamma);
		} catch (LWJGLException e) {
			LWJGLUtil.log("Failed to reset gamma ramp: " + e.getMessage());
		}
		current_gamma = saved_gamma;
		if (mode_set) {
			mode_set = false;
			nResetDisplayMode();
		}
		resetCursorClipping();
	}
	private static native void nResetDisplayMode();

	public int getGammaRampLength() {
		return GAMMA_LENGTH;
	}

	public void setGammaRamp(FloatBuffer gammaRamp) throws LWJGLException {
		doSetGammaRamp(convertToNativeRamp(gammaRamp));
	}
	private static native ByteBuffer convertToNativeRamp(FloatBuffer gamma_ramp) throws LWJGLException;
	private static native ByteBuffer getCurrentGammaRamp() throws LWJGLException;

	private void doSetGammaRamp(ByteBuffer native_gamma) throws LWJGLException {
		nSetGammaRamp(native_gamma);
		current_gamma = native_gamma;
	}
	private static native void nSetGammaRamp(ByteBuffer native_ramp) throws LWJGLException;

	public String getAdapter() {
		try {
			String adapter_string = WindowsRegistry.queryRegistrationKey(
					WindowsRegistry.HKEY_LOCAL_MACHINE,
					"HARDWARE\\DeviceMap\\Video",
					"\\Device\\Video0");
			String root_key = "\\registry\\machine\\";
			if (adapter_string.toLowerCase().startsWith(root_key)) {
				String driver_value = WindowsRegistry.queryRegistrationKey(
						WindowsRegistry.HKEY_LOCAL_MACHINE,
						adapter_string.substring(root_key.length()),
						"InstalledDisplayDrivers");
				return driver_value;
			}
		} catch (LWJGLException e) {
			LWJGLUtil.log("Exception occurred while querying registry: " + e);
		}
		return null;
	}
	
	public String getVersion() {
		String driver = getAdapter();
		if (driver != null) {
			WindowsFileVersion version = nGetVersion(driver + ".dll");
			if (version != null)
				return version.toString();
		}
		return null;
	}
	private native WindowsFileVersion nGetVersion(String driver);

	public DisplayMode init() throws LWJGLException {
		current_gamma = saved_gamma = getCurrentGammaRamp();
		return current_mode = getCurrentDisplayMode();
	}
	private static native DisplayMode getCurrentDisplayMode() throws LWJGLException;

	public native void setTitle(String title);

	public boolean isCloseRequested() {
		boolean saved = close_requested;
		close_requested = false;
		return saved;
	}

	public boolean isVisible() {
		return !isMinimized;
	}

	public boolean isActive() {
		return isFocused;
	}

	public boolean isDirty() {
		boolean saved = is_dirty;
		is_dirty = false;
		return saved;
	}

	public PeerInfo createPeerInfo(PixelFormat pixel_format) throws LWJGLException {
		peer_info = new WindowsDisplayPeerInfo(pixel_format);
		return peer_info;
	}
	public void update() {
		nUpdate();
		if (did_maximize) {
			did_maximize = false;
			/**
			 * WORKAROUND:
			 * Making the context current (redundantly) when the window
			 * is maximized helps some gfx recover from fullscreen
			 */
			try {
				if (Display.getContext().isCurrent())
					Display.getContext().makeCurrent();
			} catch (LWJGLException e) {
				LWJGLUtil.log("Exception occurred while trying to make context current: " + e);
			}
		}
	}
	private static native void nUpdate();

	public void reshape(int x, int y, int width, int height) {
		if (!isFullscreen)
			nReshape(getHwnd(), x, y, width, height);
	}
	private static native void nReshape(long hwnd, int x, int y, int width, int height);
	public native DisplayMode[] getAvailableDisplayModes() throws LWJGLException;

	/* Mouse */
	public boolean hasWheel() {
		return mouse.hasWheel();
	}

	public int getButtonCount() {
		return mouse.getButtonCount();
	}

	public void createMouse() throws LWJGLException {
		mouse = new WindowsMouse(createDirectInput(), getHwnd());
	}

	public void destroyMouse() {
		mouse.destroy();
		mouse = null;
	}

	public void pollMouse(IntBuffer coord_buffer, ByteBuffer buttons) {
		update();
		mouse.poll(coord_buffer, buttons);
	}
	
	public void readMouse(ByteBuffer buffer) {
		update();
		mouse.read(buffer);
	}
		
	public void grabMouse(boolean grab) {
		mouse.grab(grab);
	}

	public int getNativeCursorCapabilities() {
		return Cursor.CURSOR_ONE_BIT_TRANSPARENCY;
	}

	public void setCursorPosition(int x, int y) {
		nSetCursorPosition(x, y, isFullscreen);
	}
	private static native void nSetCursorPosition(int x, int y, boolean fullscreen);

	public native void setNativeCursor(Object handle) throws LWJGLException;

	public int getMinCursorSize() {
		return getSystemMetrics(SM_CXCURSOR);
	}

	public int getMaxCursorSize() {
		return getSystemMetrics(SM_CXCURSOR);
	}

	public native int getSystemMetrics(int index);

	private static native long getDllInstance();
	private static native long getHwnd();

	/* Keyboard */
	public void createKeyboard() throws LWJGLException {
		keyboard = new WindowsKeyboard(createDirectInput(), getHwnd());
	}

	public void destroyKeyboard() {
		keyboard.destroy();
		keyboard = null;
	}
	
	public void pollKeyboard(ByteBuffer keyDownBuffer) {
		update();
		keyboard.poll(keyDownBuffer);
	}
	
	public void readKeyboard(ByteBuffer buffer) {
		update();
		keyboard.read(buffer);
	}

//	public native int isStateKeySet(int key);

	public native ByteBuffer nCreateCursor(int width, int height, int xHotspot, int yHotspot, int numImages, IntBuffer images, int images_offset, IntBuffer delays, int delays_offset) throws LWJGLException;

	public Object createCursor(int width, int height, int xHotspot, int yHotspot, int numImages, IntBuffer images, IntBuffer delays) throws LWJGLException {
		return nCreateCursor(width, height, xHotspot, yHotspot, numImages, images, images.position(), delays, delays != null ? delays.position() : -1);
	}

	public native void destroyCursor(Object cursorHandle);
	public int getPbufferCapabilities() {
		try {
		// Return the capabilities of a minimum pixel format
			return nGetPbufferCapabilities(new PixelFormat(0, 0, 0, 0, 0, 0, 0, 0, false));
		} catch (LWJGLException e) {
			LWJGLUtil.log("Exception occurred while determining pbuffer capabilities: " + e);
			return 0;
		}
	}
	private native int nGetPbufferCapabilities(PixelFormat format) throws LWJGLException;
	
	public boolean isBufferLost(PeerInfo handle) {
		return ((WindowsPbufferPeerInfo)handle).isBufferLost();
	}

	public PeerInfo createPbuffer(int width, int height, PixelFormat pixel_format,
			IntBuffer pixelFormatCaps,
			IntBuffer pBufferAttribs) throws LWJGLException {
		return new WindowsPbufferPeerInfo(width, height, pixel_format, pixelFormatCaps, pBufferAttribs);
	}
	
	public void setPbufferAttrib(PeerInfo handle, int attrib, int value) {
		((WindowsPbufferPeerInfo)handle).setPbufferAttrib(attrib, value);
	}

	public void bindTexImageToPbuffer(PeerInfo handle, int buffer) {
		((WindowsPbufferPeerInfo)handle).bindTexImageToPbuffer(buffer);
	}
	
	public void releaseTexImageFromPbuffer(PeerInfo handle, int buffer) {
		((WindowsPbufferPeerInfo)handle).releaseTexImageFromPbuffer(buffer);
	}
	

	/**
	 * Sets one or more icons for the Display.
	 * <ul>
	 * <li>On Windows you should supply at least one 16x16 icon and one 32x32.</li>
	 * <li>Linux (and similar platforms) expect one 32x32 icon.</li>
	 * <li>Mac OS X should be supplied one 128x128 icon</li>
	 * </ul>
	 * The implementation will use the supplied ByteBuffers with image data in RGBA and perform any conversions nescesarry for the specific platform.
	 *
	 * @param icons Array of icons in RGBA mode
	 * @return number of icons used.
	 */
	public int setIcon(ByteBuffer[] icons) {
		boolean done16 = false;
		boolean done32 = false;
		int used = 0;
		
		for (int i=0;i<icons.length;i++) {
			int size = icons[i].limit() / 4;
			
			if ((((int) Math.sqrt(size)) == 16) && (!done16)) {
				nSetWindowIcon16(icons[i].asIntBuffer());
				used++;
				done16 = true;
			}
			if ((((int) Math.sqrt(size)) == 32) && (!done32)) {
				nSetWindowIcon32(icons[i].asIntBuffer());
				used++;
				done32 = true;
			}
		}
		
		return used;
	}

	private static native int nSetWindowIcon16(IntBuffer icon);
	
	private static native int nSetWindowIcon32(IntBuffer icon);

	private void handleMouseButton(int button, int state, long millis) {
		if (mouse != null)
			mouse.handleMouseButton((byte)button, (byte)state, millis);
	}

	private void handleMouseMoved(int x, int y, long millis) {
		if (mouse != null)
			mouse.handleMouseMoved(x, y, millis);
	}

	private void handleMouseScrolled(int amount, long millis) {
		if (mouse != null)
			mouse.handleMouseScrolled(amount, millis);
	}

	private static native int transformY(long hwnd, int y);

	private static boolean handleMessage(long hwnd, int msg, long wParam, long lParam, long millis) {
		if (current_display != null)
			return current_display.doHandleMessage(hwnd, msg, wParam, lParam, millis);
		else
			return false;
	}

	private boolean doHandleMessage(long hwnd, int msg, long wParam, long lParam, long millis) {
		if (isFullscreen && !isMinimized && isFocused) {
			try {
				setupCursorClipping(getHwnd());
			} catch (LWJGLException e) {
				LWJGLUtil.log("setupCursorClipping failed: " + e.getMessage());
			}
		}
		switch (msg) {
			// disable screen saver and monitor power down messages which wreak havoc
			case WM_ACTIVATE:
				switch ((int)wParam) {
					case WA_ACTIVE:
					case WA_CLICKACTIVE:
						appActivate(true);
						break;
					case WA_INACTIVE:
						appActivate(false);
						break;
				}
				return true;
			case WM_SIZE:
				switch ((int)wParam) {
					case SIZE_RESTORED:
					case SIZE_MAXIMIZED:
						isMinimized = false;
						break;
					case SIZE_MINIMIZED:
						isMinimized = true;
						break;
				}
				return false;
			case WM_MOUSEMOVE:
				int xPos = (int)(short)(lParam & 0xFFFF);
				int yPos = transformY(getHwnd(), (int)(short)((lParam >> 16) & 0xFFFF));
				handleMouseMoved(xPos, yPos, millis);
				return true;
			case WM_MOUSEWHEEL:
				int dwheel = (int)(short)((wParam >> 16) & 0xFFFF);
				handleMouseScrolled(dwheel, millis);
				return true;
			case WM_LBUTTONDOWN:
				handleMouseButton(0, 1, millis);
				return true;
			case WM_LBUTTONUP:
				handleMouseButton(0, 0, millis);
				return true;
			case WM_RBUTTONDOWN:
				handleMouseButton(1, 1, millis);
				return true;
			case WM_RBUTTONUP:
				handleMouseButton(1, 0, millis);
				return true;
			case WM_MBUTTONDOWN:
				handleMouseButton(2, 1, millis);
				return true;
			case WM_MBUTTONUP:
				handleMouseButton(2, 0, millis);
				return true;
			case WM_QUIT:
				close_requested = true;
				return true;
			case WM_SYSCOMMAND:
				switch ((int)wParam) {
					case SC_KEYMENU:
					case SC_MOUSEMENU:
					case SC_SCREENSAVE:
					case SC_MONITORPOWER:
						return true;
					case SC_CLOSE:
						close_requested = true;
						return true;
					default:
						break;
				}
				return false;
			case WM_PAINT:
				is_dirty = true;
				return false;
			default:
				return false;
		}
	}

	private static WindowsDirectInput createDirectInput() throws LWJGLException {
		try {
			return new WindowsDirectInput8(getDllInstance());
		} catch (LWJGLException e) {
			LWJGLUtil.log("Failed to create DirectInput 8 interface, falling back to DirectInput 3");
			return new WindowsDirectInput3(getDllInstance());
		}
	}
}
