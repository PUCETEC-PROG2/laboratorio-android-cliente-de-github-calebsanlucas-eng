package ec.edu.uisek.githubclient.models

import com.google.gson.annotations.SerializedName


class RepoOwner (
    val id: Long,
    val login: String,
    @SerializedName(value = "avatar_url")
    val avatarURL: String,)

data class RepoUpdateRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?
)