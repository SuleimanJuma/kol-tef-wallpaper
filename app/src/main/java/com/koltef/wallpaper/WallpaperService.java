
package com.koltef.wallpaper;

import android.view.SurfaceHolder;
import android.view.View;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

public class WallpaperService extends android.service.wallpaper.WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends Engine {

        private final Handler handler =
                new Handler(Looper.getMainLooper());

        private WebView webView;
        private Bitmap bitmap;
        private Canvas bitmapCanvas;

        private boolean visible = false;
        private boolean surfaceReady = false;

        private int width = 1;
        private int height = 1;

        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                drawFrame();

                if (visible && surfaceReady) {
                    handler.postDelayed(this, 33);
                }
            }
        };

        WallpaperEngine() {
            createWebView();
        }

        private void createWebView() {

            webView = new WebView(WallpaperService.this);

            WebSettings settings = webView.getSettings();

            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setMediaPlaybackRequiresUserGesture(false);

            webView.setBackgroundColor(Color.BLACK);
            webView.setWebViewClient(new WebViewClient());

            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            webView.loadUrl("file:///android_asset/index.html");
        }

        private void resizeWebView() {

            if (webView == null) return;

            webView.measure(
                    View.MeasureSpec.makeMeasureSpec(
                            width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(
                            height, View.MeasureSpec.EXACTLY)
            );

            webView.layout(0, 0, width, height);

            if (bitmap != null) {
                bitmap.recycle();
            }

            bitmap = Bitmap.createBitmap(
                    width, height, Bitmap.Config.ARGB_8888
            );

            bitmapCanvas = new Canvas(bitmap);
        }

        private void drawFrame() {

            if (!visible || !surfaceReady ||
                    webView == null || bitmapCanvas == null) {
                return;
            }

            SurfaceHolder holder = getSurfaceHolder();

            if (!holder.getSurface().isValid()) return;

            try {

                bitmapCanvas.drawColor(Color.BLACK);

                webView.draw(bitmapCanvas);

                Canvas canvas = holder.lockCanvas();

                if (canvas != null) {
                    try {
                        canvas.drawColor(Color.BLACK);

                        canvas.drawBitmap(
                                bitmap,
                                null,
                                canvas.getClipBounds(),
                                null
                        );

                    } finally {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }

            } catch (Exception e) {
                android.util.Log.e(
                        "KOL-TEF", "Rendering error", e
                );
            }
        }

        @Override
        public void onSurfaceChanged(
                SurfaceHolder holder,
                int format,
                int width,
                int height
        ) {
            super.onSurfaceChanged(holder, format, width, height);

            this.width = width;
            this.height = height;

            surfaceReady = true;

            resizeWebView();

            if (visible) {
                handler.removeCallbacks(drawRunner);
                handler.post(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);

            surfaceReady = false;
            handler.removeCallbacks(drawRunner);
        }

        @Override
        public void onVisibilityChanged(boolean isVisible) {
            visible = isVisible;

            handler.removeCallbacks(drawRunner);

            if (visible && surfaceReady) {
                handler.post(drawRunner);
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {

            if (webView != null) {
                MotionEvent copy = MotionEvent.obtain(event);

                webView.dispatchTouchEvent(copy);

                copy.recycle();
            }

            super.onTouchEvent(event);
        }

        @Override
        public void onDestroy() {

            visible = false;
            surfaceReady = false;

            handler.removeCallbacks(drawRunner);

            if (webView != null) {
                webView.stopLoading();
                webView.destroy();
                webView = null;
            }

            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }

            bitmapCanvas = null;

            super.onDestroy();
        }
    }
}

