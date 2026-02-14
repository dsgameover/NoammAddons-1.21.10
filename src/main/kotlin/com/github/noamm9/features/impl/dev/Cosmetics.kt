package com.github.noamm9.features.impl.dev

/*
@AlwaysActive
object Cosmetics: Feature() {
    val cosmeticPeople by lazy { DataDownloader.loadJson<Map<UUID, CosmeticData>>("cosmeticPeople.json") }

    @JvmStatic
    fun extractRenderStateHook(avatar: Avatar, state: AvatarRenderState) {
        if (avatar !is AbstractClientPlayer) return
        val data = cosmeticPeople[avatar.gameProfile.id] ?: return
        state.setData<GameProfile>(GAME_PROFILE_KEY, avatar.gameProfile)

        if (data.hasCustomName) state.nameTag = Component.literal(data.customName.addColor())
    }

    @JvmStatic
    fun preRenderCallbackScaleHook(state: AvatarRenderState, poseStack: PoseStack) {
        val gameProfile = state.getData(GAME_PROFILE_KEY) ?: return
        val data = cosmeticPeople[gameProfile.id] ?: return
        if (! data.hasCustomSize) return

        if (data.sizeY < 0) poseStack.translate(.0, data.sizeY * 2.0, .0)
        poseStack.scale(data.sizeX, data.sizeY, data.sizeZ)
    }

    @JvmField
    val GAME_PROFILE_KEY: RenderStateDataKey<GameProfile> = RenderStateDataKey.create { "${NoammAddons.MOD_ID}:game_profile" }

    data class CosmeticData(
        val customName: String = "",
        val sizeX: Float = 1f,
        val sizeY: Float = 1f,
        val sizeZ: Float = 1f,
    ) {
        val hasCustomSize: Boolean get() = sizeX != 1.0f || sizeY != 1.0f || sizeZ != 1.0f
        val hasCustomName: Boolean get() = customName.isNotEmpty()
    }
}*/