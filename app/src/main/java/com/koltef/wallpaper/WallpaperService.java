package com.koltef.wallpaper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.Random;

public class WallpaperService extends android.service.wallpaper.WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends Engine {

        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Random random = new Random();

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint backgroundPaint = new Paint();

        private final String[] themes = {
            "#00ff66", "#00f0ff", "#ff9900"
        };

        private final String[] fields = {
            "FARMING & AGRICULTURE",
            "TEACHING & EDUCATION",
            "FAMILY & COMMUNITY",
            "SOCIETY & NATIONS",
            "TECHNOLOGY & CODE",
            "GOVERNANCE & STEWARDSHIP"
        };

        private final String[] tags = {
            "GROWTH // SEED // HARVEST // SUSTENANCE",
            "KNOWLEDGE // WISDOM // DISCIPLESHIP",
            "FOUNDATION // UNITY // GENERATIONS",
            "PEACE // ORDER // LEADERSHIP",
            "LOGIC // CREATION // ENGINEERING",
            "JUSTICE // INTEGRITY // RIGHTEOUSNESS"
        };

        private final String[] binaries = {
            "01000110 01000001 01010010 01001101 01001001 01001110 01000111",
            "01010100 01000101 01000001 01000011 01001000 01001001 01001110 01000111",
            "01000110 01000001 01001101 01001001 01001100 01001001 01000101 01010011",
            "01010011 01001111 01000011 01001001 01000101 01010011 01010100 01011001",
            "01010100 01000101 01000011 01001000 01001110 01001111 01001100 01001111 01000111 01001001 01000101 01010011",
            "01000111 01001111 01010110 01000101 01010010 01001110"
        };

        private final String motto =
            "HAKKOL TEFILLAH (EVERYTHING BY PRAYER)";

        private final String mottoBinary =
            "01101000 01100001 01101011 01101011 01101111 01101100 " +
            "00100000 01110100 01100101 01100110 01101001 01101100 " +
            "01101100 00100000 01110000 01100001 01100001 01111001 " +
            "01100101 01100010";

        private boolean visible;
        private boolean surfaceReady;
        private boolean hasDrawn;
        private int width = 1;
        private int height = 1;

        private int themeIndex = 0;
        private int fieldIndex = 0;
        private int stage = 0;
        private int loopCount = 1;

        private float[] drops;
        private float[] speeds;
        private int fontSize = 14;
        private float speedMultiplier = 1.0f;

        private long stageStart;
        private long lastTouch;
        private float touchStartX;
        private float touchStartY;

        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                if (!visible || !surfaceReady) return;

                drawFrame();

                if (visible && surfaceReady) {
                    handler.postDelayed(this, 33);
                }
            }
        };

        WallpaperEngine() {
            setTouchEventsEnabled(true);
            stageStart = SystemClock.uptimeMillis();
            paint.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
            backgroundPaint.setColor(Color.BLACK);
        }

        private int themeColor() {
            return Color.parseColor(themes[themeIndex]);
        }

        private void initializeMatrix() {
            fontSize = Math.max(12, Math.min(18, width / 30));
            int columns = Math.max(1, width / fontSize);

            drops = new float[columns];
            speeds = new float[columns];

            for (int i = 0; i < columns; i++) {
                drops[i] = -random.nextInt(50);
                speeds[i] = 0.6f + random.nextFloat() * 0.8f;
            }
        }

        private void drawFrame() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();

                if (canvas == null) return;

                if (!hasDrawn) {
                    canvas.drawColor(Color.BLACK);
                    hasDrawn = true;
                } else {
                    canvas.drawColor(Color.argb(22, 0, 0, 0));
                }

                drawMatrix(canvas);
                updateStage();
                drawInterface(canvas);

            } catch (Exception e) {
                android.util.Log.e("KOL-TEF", "Wallpaper drawing failed", e);
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void drawMatrix(Canvas canvas) {
            paint.setTextAlign(Paint.Align.LEFT);
            if (drops == null) return;

            int color = themeColor();

            paint.setTextSize(fontSize);
            paint.setColor(color);
            paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

            for (int i = 0; i < drops.length; i++) {
                float x = i * fontSize;
                float y = drops[i] * fontSize;

                if (random.nextFloat() > 0.90f) {
                    paint.setColor(Color.WHITE);
                    paint.setShadowLayer(8, 0, 0, color);
                } else {
                    paint.setColor(color);
                    paint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
                }

                canvas.drawText(
                    random.nextBoolean() ? "1" : "0",
                    x, y, paint
                );

                if (y > height && random.nextFloat() > 0.975f) {
                    drops[i] = 0;
                }

                drops[i] += speeds[i] * speedMultiplier;
            }

            paint.clearShadowLayer();
        }

        private void updateStage() {
            long elapsed = SystemClock.uptimeMillis() - stageStart;

            if (stage == 0) {
                long typingDuration = motto.length() * 55L;

                if (elapsed >= typingDuration + 2200L) {
                    stage = 1;
                    stageStart = SystemClock.uptimeMillis();
                }

            } else if (stage == 1) {
                if (elapsed >= 880L) {
                    stage = 2;
                    fieldIndex = 0;
                    stageStart = SystemClock.uptimeMillis();
                }

            } else if (stage == 2) {
                if (elapsed >= 5500L) {
                    if (fieldIndex < fields.length - 1) {
                        fieldIndex++;
                    } else {
                        stage = 0;
                        loopCount++;
                    }

                    stageStart = SystemClock.uptimeMillis();
                }
            }
        }

        private void drawInterface(Canvas canvas) {
            int color = themeColor();

            paint.setColor(color);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.create("monospace", Typeface.BOLD));
            paint.setShadowLayer(18, 0, 0, color);

            float centerX = width / 2f;

            paint.setTextSize(Math.min(width * 0.105f, 54));
            canvas.drawText("KOL-TEF", centerX, height * 0.22f, paint);

            paint.clearShadowLayer();
            paint.setTypeface(Typeface.create("monospace", Typeface.NORMAL));

            if (stage == 0) {
                drawStageOne(canvas, centerX);
            } else if (stage == 1) {
                drawGlitch(canvas, centerX);
            } else {
                drawField(canvas, centerX);
            }

            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(10);
            paint.setColor(color);
            canvas.drawText("KOL_TEF // LIVE SYSTEM", 14, height - 34, paint);
            canvas.drawText("LOOP #" + loopCount, 14, height - 18, paint);

            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(
                "THEME: " + new String[]{"GREEN", "CYAN", "AMBER"}[themeIndex],
                width - 14, height - 18, paint
            );

            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(9);
            paint.setColor(Color.LTGRAY);
            canvas.drawText(
                "TAP: THEME  //  SWIPE: FIELD",
                centerX, height - 52, paint
            );
        }

        private void drawStageOne(Canvas canvas, float centerX) {
            long elapsed = SystemClock.uptimeMillis() - stageStart;
            int typed = (int) Math.min(motto.length(), elapsed / 55L);
            String visibleText = motto.substring(0, typed);

            paint.setColor(themeColor());
            paint.setTextSize(Math.min(width * 0.035f, 15));
            canvas.drawText(
                visibleText,
                centerX,
                height * 0.34f,
                paint
            );

            String binary = mottoBinary;
            int binaryLength = (int) (
                (binary.length() * (float) typed) /
                Math.max(1, motto.length())
            );

            paint.setTextSize(9);
            paint.setColor(Color.LTGRAY);
            drawWrappedText(
                canvas,
                binary.substring(0, Math.min(binaryLength, binary.length())),
                centerX,
                height * 0.40f,
                width * 0.82f,
                15
            );

            paint.setTextSize(10);
            paint.setColor(themeColor());
            canvas.drawText(
                "STAGE 1 // STREAMING MOTTO",
                centerX,
                height * 0.48f,
                paint
            );
        }

        private void drawGlitch(Canvas canvas, float centerX) {
            paint.setTextSize(18);
            paint.setColor(themeColor());
            paint.setShadowLayer(12, 0, 0, themeColor());

            StringBuilder noise = new StringBuilder();

            for (int i = 0; i < 18; i++) {
                noise.append(random.nextBoolean() ? "1" : "0");
            }

            canvas.drawText(noise.toString(), centerX, height * 0.37f, paint);
            paint.clearShadowLayer();

            paint.setTextSize(11);
            canvas.drawText(
                "SIGNAL TRANSITION // DECRYPTING",
                centerX,
                height * 0.43f,
                paint
            );
        }

        private void drawField(Canvas canvas, float centerX) {
            float top = height * 0.34f;
            float cardWidth = width * 0.86f;
            float left = (width - cardWidth) / 2f;

            paint.setColor(Color.argb(205, 0, 0, 0));
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(
                left, top, left + cardWidth, top + height * 0.28f,
                16, 16, paint
            );

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(themeColor());
            canvas.drawRoundRect(
                left, top, left + cardWidth, top + height * 0.28f,
                16, 16, paint
            );
            paint.setStyle(Paint.Style.FILL);

            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(10);
            paint.setColor(Color.LTGRAY);

            canvas.drawText(
                "STAGE 2 // PRAYER IN ALL FIELDS",
                centerX, top + 25, paint
            );

            paint.setTextSize(12);
            paint.setColor(themeColor());

            canvas.drawText(
                "[REDACTED FIELD " + (fieldIndex + 1) + "]",
                centerX, top + 65, paint
            );

            paint.setTextSize(8);
            paint.setColor(Color.LTGRAY);

            canvas.drawText(
                tags[fieldIndex],
                centerX, top + 88, paint
            );

            paint.setTextSize(9);
            paint.setColor(themeColor());

            drawWrappedText(
                canvas,
                binaries[fieldIndex],
                centerX,
                top + 120,
                cardWidth - 24,
                16
            );

            paint.setColor(Color.DKGRAY);
            canvas.drawRect(
                left + 12, top + height * 0.245f,
                left + cardWidth - 12, top + height * 0.245f + 4,
                paint
            );

            float progress = Math.min(
                1f,
                (SystemClock.uptimeMillis() - stageStart) / 5500f
            );

            paint.setColor(themeColor());
            canvas.drawRect(
                left + 12, top + height * 0.245f,
                left + 12 + (cardWidth - 24) * progress,
                top + height * 0.245f + 4,
                paint
            );

            paint.setTextSize(9);
            paint.setColor(Color.LTGRAY);

            canvas.drawText(
                (fieldIndex + 1) + " / " + fields.length,
                centerX, top + height * 0.28f - 8,
                paint
            );
        }

        private void drawWrappedText(
            Canvas canvas,
            String text,
            float centerX,
            float startY,
            float maxWidth,
            float lineHeight
        ) {
            if (text == null || text.isEmpty()) return;

            StringBuilder line = new StringBuilder();
            float y = startY;

            for (String word : text.split(" ")) {
                String candidate = line.length() == 0
                    ? word : line + " " + word;

                if (paint.measureText(candidate) > maxWidth &&
                    line.length() > 0) {
                    canvas.drawText(line.toString(), centerX, y, paint);
                    y += lineHeight;
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }

            if (line.length() > 0) {
                canvas.drawText(line.toString(), centerX, y, paint);
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
            hasDrawn = false;

            initializeMatrix();

            handler.removeCallbacks(drawRunner);

            if (visible) {
                handler.post(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            surfaceReady = false;
            handler.removeCallbacks(drawRunner);
            super.onSurfaceDestroyed(holder);
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
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                touchStartX = event.getX();
                touchStartY = event.getY();
                lastTouch = SystemClock.uptimeMillis();

            } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                float dx = event.getX() - touchStartX;
                float dy = event.getY() - touchStartY;

                if (Math.abs(dx) > 70 && Math.abs(dx) > Math.abs(dy)) {
                    if (dx < 0) {
                        fieldIndex = (fieldIndex + 1) % fields.length;
                    } else {
                        fieldIndex = (fieldIndex - 1 + fields.length) % fields.length;
                    }

                    stage = 2;
                    stageStart = SystemClock.uptimeMillis();

                } else if (SystemClock.uptimeMillis() - lastTouch < 500) {
                    themeIndex = (themeIndex + 1) % themes.length;
                }
            }

            super.onTouchEvent(event);
        }

        @Override
        public void onDestroy() {
            visible = false;
            surfaceReady = false;
            handler.removeCallbacks(drawRunner);
            super.onDestroy();
        }
    }
}
