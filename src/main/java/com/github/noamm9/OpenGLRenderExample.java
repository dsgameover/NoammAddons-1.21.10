package com.github.noamm9;
/*

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class OpenGLRenderExample {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final String MOD_ID = "hunchclient";

    public static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(ResourceLocation.fromNamespaceAndPath(MOD_ID, "pipeline/filled_through_walls"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    );

    public static final RenderPipeline LINES_THROUGH_WALLS = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(ResourceLocation.fromNamespaceAndPath(MOD_ID, "pipeline/lines_through_walls"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    );

    public static final RenderPipeline QUADS_THROUGH_WALLS = RenderPipelines.register(
        RenderPipeline.builder(DefaultVertexFormat.POSITION_COLOR)
            .withLocation(ResourceLocation.fromNamespaceAndPath(MOD_ID, "pipeline/quads_through_walls"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build()
    );

    public static void drawFilledBox(Vec3 cameraPos, AABB box, float r, float g, float b, float a, boolean throughWalls) {
        RenderPipeline pipeline = throughWalls ? FILLED_THROUGH_WALLS : RenderPipelines.DEBUG_FILLED_BOX;
        BufferBuilder buffer = getBuffer(pipeline);

        Matrix4f positionMatrix = new Matrix4f()
            .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        ShapeRenderer.addChainedFilledBoxVertices(
            toStack(positionMatrix),
            buffer,
            (float) box.minX, (float) box.minY, (float) box.minZ,
            (float) box.maxX, (float) box.maxY, (float) box.maxZ,
            r, g, b, a
        );
    }

    public static void drawLine(Vec3 cameraPos, Vec3 start, Vec3 end, float r, float g, float b, float a, float lineWidth, boolean throughWalls) {
        RenderPipeline pipeline = throughWalls ? LINES_THROUGH_WALLS : RenderPipelines.LINES;
        BufferBuilder buffer = getBuffer(pipeline, lineWidth);

        Matrix4f positionMatrix = new Matrix4f()
            .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        Vector3f normalVec = end.toVector3f()
            .sub((float) start.x, (float) start.y, (float) start.z)
            .normalize();

        buffer.addVertex(positionMatrix, (float) start.x, (float) start.y, (float) start.z)
            .setColor(r, g, b, a)
            .setNormal(normalVec.x(), normalVec.y(), normalVec.z());

        buffer.addVertex(positionMatrix, (float) end.x, (float) end.y, (float) end.z)
            .setColor(r, g, b, a)
            .setNormal(normalVec.x(), normalVec.y(), normalVec.z());
    }

    public static void drawLines(Vec3 cameraPos, Vec3[] points, float r, float g, float b, float a, float lineWidth, boolean throughWalls) {
        RenderPipeline pipeline = throughWalls ? LINES_THROUGH_WALLS : RenderPipelines.LINES;
        BufferBuilder buffer = getBuffer(pipeline, lineWidth);

        Matrix4f positionMatrix = new Matrix4f()
            .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        for (int i = 0; i < points.length - 1; i++) {
            Vec3 current = points[i];
            Vec3 next = points[i + 1];

            Vector3f normalVec = next.toVector3f()
                .sub((float) current.x, (float) current.y, (float) current.z)
                .normalize();

            buffer.addVertex(positionMatrix, (float) current.x, (float) current.y, (float) current.z)
                .setColor(r, g, b, a)
                .setNormal(normalVec.x(), normalVec.y(), normalVec.z());

            buffer.addVertex(positionMatrix, (float) next.x, (float) next.y, (float) next.z)
                .setColor(r, g, b, a)
                .setNormal(normalVec.x(), normalVec.y(), normalVec.z());
        }
    }

    public static void drawOutlinedBox(Vec3 cameraPos, AABB box, float r, float g, float b, float a, float lineWidth, boolean throughWalls) {
        Vec3[] corners = {
            new Vec3(box.minX, box.minY, box.minZ),
            new Vec3(box.maxX, box.minY, box.minZ),
            new Vec3(box.maxX, box.minY, box.maxZ),
            new Vec3(box.minX, box.minY, box.maxZ),
            new Vec3(box.minX, box.maxY, box.minZ),
            new Vec3(box.maxX, box.maxY, box.minZ),
            new Vec3(box.maxX, box.maxY, box.maxZ),
            new Vec3(box.minX, box.maxY, box.maxZ)
        };

        int[][] edges = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        RenderPipeline pipeline = throughWalls ? LINES_THROUGH_WALLS : RenderPipelines.LINES;
        BufferBuilder buffer = getBuffer(pipeline, lineWidth);

        Matrix4f positionMatrix = new Matrix4f()
            .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        for (int[] edge : edges) {
            Vec3 start = corners[edge[0]];
            Vec3 end = corners[edge[1]];

            Vector3f normalVec = end.toVector3f()
                .sub((float) start.x, (float) start.y, (float) start.z)
                .normalize();

            buffer.addVertex(positionMatrix, (float) start.x, (float) start.y, (float) start.z)
                .setColor(r, g, b, a)
                .setNormal(normalVec.x(), normalVec.y(), normalVec.z());

            buffer.addVertex(positionMatrix, (float) end.x, (float) end.y, (float) end.z)
                .setColor(r, g, b, a)
                .setNormal(normalVec.x(), normalVec.y(), normalVec.z());
        }
    }

    public static void drawQuad(Vec3 cameraPos, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, float r, float g, float b, float a, boolean throughWalls) {
        RenderPipeline pipeline = throughWalls ? QUADS_THROUGH_WALLS : RenderPipelines.DEBUG_QUADS;
        BufferBuilder buffer = getBuffer(pipeline);

        Matrix4f matrix = new Matrix4f()
            .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        buffer.addVertex(matrix, (float) p1.x, (float) p1.y, (float) p1.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) p2.x, (float) p2.y, (float) p2.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) p3.x, (float) p3.y, (float) p3.z).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) p4.x, (float) p4.y, (float) p4.z).setColor(r, g, b, a);
    }

    public static void drawCircle(Vec3 cameraPos, Vec3 center, float radius, int segments, float r, float g, float b, float a, boolean throughWalls) {
        RenderPipeline pipeline = throughWalls ? LINES_THROUGH_WALLS : RenderPipelines.LINES;
        BufferBuilder buffer = getBuffer(pipeline, 2.0f);

        Matrix4f positionMatrix = new Matrix4f()
            .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

            float x1 = (float) center.x + radius * (float) Math.cos(angle1);
            float z1 = (float) center.z + radius * (float) Math.sin(angle1);
            float x2 = (float) center.x + radius * (float) Math.cos(angle2);
            float z2 = (float) center.z + radius * (float) Math.sin(angle2);

            Vector3f normal = new Vector3f(0, 1, 0);

            buffer.addVertex(positionMatrix, x1, (float) center.y, z1)
                .setColor(r, g, b, a)
                .setNormal(normal.x(), normal.y(), normal.z());

            buffer.addVertex(positionMatrix, x2, (float) center.y, z2)
                .setColor(r, g, b, a)
                .setNormal(normal.x(), normal.y(), normal.z());
        }
    }

    public static void drawCylinder(Vec3 cameraPos, Vec3 center, float radius, float height, int segments, float r, float g, float b, float a) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = new Matrix4f()
            .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        for (int i = 0; i <= segments; i++) {
            float angle = (float) (2 * Math.PI * i / segments);
            float x = (float) center.x + radius * (float) Math.cos(angle);
            float z = (float) center.z + radius * (float) Math.sin(angle);

            buffer.addVertex(matrix, x, (float) center.y, z).setColor(r, g, b, a);
            buffer.addVertex(matrix, x, (float) center.y + height, z).setColor(r, g, b, a);
        }

        drawAndEndBuffer(buffer);
    }

    public static void drawBeam(Vec3 cameraPos, Vec3 start, Vec3 end, float thickness, float r, float g, float b, float a) {
        RenderPipeline pipeline = LINES_THROUGH_WALLS;
        BufferBuilder buffer = getBuffer(pipeline, thickness);

        Matrix4f positionMatrix = new Matrix4f()
            .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        Vector3f direction = end.toVector3f()
            .sub((float) start.x, (float) start.y, (float) start.z)
            .normalize();

        buffer.addVertex(positionMatrix, (float) start.x, (float) start.y, (float) start.z)
            .setColor(r, g, b, a)
            .setNormal(direction.x(), direction.y(), direction.z());

        buffer.addVertex(positionMatrix, (float) end.x, (float) end.y, (float) end.z)
            .setColor(r, g, b, a)
            .setNormal(direction.x(), direction.y(), direction.z());
    }

    public static void drawGradientLine(Vec3 cameraPos, Vec3 start, Vec3 end, float thickness,
                                        float r1, float g1, float b1, float a1,
                                        float r2, float g2, float b2, float a2) {
        RenderPipeline pipeline = LINES_THROUGH_WALLS;
        BufferBuilder buffer = getBuffer(pipeline, thickness);

        Matrix4f positionMatrix = new Matrix4f()
            .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        Vector3f direction = end.toVector3f()
            .sub((float) start.x, (float) start.y, (float) start.z)
            .normalize();

        buffer.addVertex(positionMatrix, (float) start.x, (float) start.y, (float) start.z)
            .setColor(r1, g1, b1, a1)
            .setNormal(direction.x(), direction.y(), direction.z());

        buffer.addVertex(positionMatrix, (float) end.x, (float) end.y, (float) end.z)
            .setColor(r2, g2, b2, a2)
            .setNormal(direction.x(), direction.y(), direction.z());
    }

    private static BufferBuilder getBuffer(RenderPipeline pipeline) {
        return Tesselator.getInstance().begin(pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
    }

    private static BufferBuilder getBuffer(RenderPipeline pipeline, float lineWidth) {
        RenderSystem.lineWidth(lineWidth);
        return Tesselator.getInstance().begin(pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
    }

    private static void drawAndEndBuffer(BufferBuilder buffer) {
        MeshData meshData = buffer.buildOrThrow();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        meshData.close();

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static PoseStack toStack(Matrix4f matrix) {
        PoseStack stack = new PoseStack();
        stack.mulPose(matrix);
        return stack;
    }

    public static void rawGLFilledQuad(float x1, float y1, float x2, float y2, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(r, g, b, a);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x1, y2);
        GL11.glEnd();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    public static void rawGLLine3D(Vec3 start, Vec3 end, float r, float g, float b, float a, float lineWidth) {
        GlStateManager._disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.setShaderColor(r, g, b, a);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(start.x, start.y, start.z);
        GL11.glVertex3d(end.x, end.y, end.z);
        GL11.glEnd();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        GlStateManager._enableDepthTest();
    }

    public static void enableStencil() {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
    }

    public static void stencilWrite(int value) {
        GL11.glStencilFunc(GL11.GL_ALWAYS, value, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glStencilMask(0xFF);
    }

    public static void stencilRead(int value) {
        GL11.glStencilFunc(GL11.GL_EQUAL, value, 0xFF);
        GL11.glStencilMask(0x00);
    }

    public static void disableStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    public static int createFramebuffer(int width, int height) {
        int fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);

        int colorTex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTex, 0);

        int depthTex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH24_STENCIL8, width, height, 0, GL30.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8, (ByteBuffer) null);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTex, 0);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        return fbo;
    }

    public static void bindFramebuffer(int fbo) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
    }

    public static void unbindFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public static void deleteFramebuffer(int fbo) {
        GL30.glDeleteFramebuffers(fbo);
    }

    public static void setBlendFunc(int srcFactor, int dstFactor) {
        RenderSystem.blendFunc(srcFactor, dstFactor);
    }

    public static void setBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        RenderSystem.blendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    public static int colorToInt(float r, float g, float b, float a) {
        int ai = (int) (a * 255) & 0xFF;
        int ri = (int) (r * 255) & 0xFF;
        int gi = (int) (g * 255) & 0xFF;
        int bi = (int) (b * 255) & 0xFF;
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }

    public static float[] intToColor(int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        return new float[]{r, g, b, a};
    }
}
*/