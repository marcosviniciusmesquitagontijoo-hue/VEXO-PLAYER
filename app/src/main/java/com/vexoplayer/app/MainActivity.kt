private fun getJson(url: String): String {
    val request = Request.Builder().url(url).get().build()
    httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IllegalStateException("Servidor respondeu HTTP ${response.code}.")
        return response.body?.string().orEmpty()
    }
}

private fun validateXtream(server: String, user: String, pass: String): Pair<Boolean, String> {
    return try {
        val json = JSONObject(getJson(apiUrl(server, user, pass)))
        val info = json.optJSONObject("user_info")
        if (info?.optString("auth") == "1") {
            val status = info.optString("status", "Active")
            if (status.equals("Active", true)) Pair(true, "")
            else Pair(false, "A conta não está ativa: $status.")
        } else Pair(false, "Login não validado. Confira os dados e a compatibilidade da API.")
    } catch (e: Exception) {
        Pair(false, e.message ?: "Não foi possível conectar ao servidor.")
    }
}

private fun loadCatalog(
    creds: XtreamCredentials,
    section: String
): Triple<List<Pair<String, String>>, List<MediaItem>, String> {
    return try {
        val (catAction, itemAction, idKey) = when (section) {
            "TV AO VIVO" -> Triple("get_live_categories", "get_live_streams", "stream_id")
            "FILMES" -> Triple("get_vod_categories", "get_vod_streams", "stream_id")
            else -> Triple("get_series_categories", "get_series", "series_id")
        }
        val catJson = getJson(apiUrl(creds.server, creds.username, creds.password, catAction))
        val itemJson = getJson(apiUrl(creds.server, creds.username, creds.password, itemAction))
        val cats = parseArray(catJson).mapNotNull { obj ->
            val id = obj.optString("category_id")
            val name = obj.optString("category_name")
            if (id.isNotBlank() && name.isNotBlank()) id to name else null
        }
        val items = parseArray(itemJson).mapNotNull { obj ->
            val id = obj.optString(idKey).ifBlank { obj.optString("id") }
            val name = obj.optString("name")
            if (id.isBlank() || name.isBlank()) null else MediaItem(
                id = id,
                name = name,
                categoryId = obj.optString("category_id"),
                streamType = when (section) {
                    "TV AO VIVO" -> "live"
                    "FILMES" -> "movie"
                    else -> "series"
                },
                containerExtension = obj.optString("container_extension", "mp4"),
                cover = obj.optString("stream_icon").ifBlank { obj.optString("cover") },
                plot = obj.optString("plot")
            )
        }
        Triple(cats, items, if (items.isEmpty()) "O servidor respondeu, mas não retornou itens para esta seção." else "")
    } catch (e: Exception) {
        Triple(emptyList(), emptyList(), e.message ?: "Falha ao carregar catálogo.")
    }
}

private fun parseArray(raw: String): List<JSONObject> {
    val array = JSONArray(raw)
    return (0 until array.length()).mapNotNull { array.optJSONObject(it) }
}

private fun loadEpisodes(creds: XtreamCredentials, seriesId: String): List<MediaItem> {
    return try {
        val json = JSONObject(
            getJson(apiUrl(creds.server, creds.username, creds.password, "get_series_info", mapOf("series_id" to seriesId)))
        )
        val episodes = json.optJSONObject("episodes") ?: return emptyList()
        val result = mutableListOf<MediaItem>()
        val seasons = episodes.keys()
        while (seasons.hasNext()) {
            val season = episodes.optJSONArray(seasons.next()) ?: continue
            for (i in 0 until season.length()) {
                val obj = season.optJSONObject(i) ?: continue
                val id = obj.optString("id")
                val name = obj.optString("title").ifBlank { obj.optString("episode_num", "Episódio") }
                if (id.isNotBlank()) result.add(
                    MediaItem(
                        id = id,
                        name = name,
                        streamType = "episode",
                        containerExtension = obj.optString("container_extension", "mp4")
                    )
                )
            }
        }
        result
    } catch (_: Exception) {
        emptyList()
    }
}

private fun buildStreamUrl(
    server: String,
    username: String,
    password: String,
    item: MediaItem,
    section: String
): String {
    val base = server.trim().trimEnd('/')
    val user = URLEncoder.encode(username, "UTF-8")
    val pass = URLEncoder.encode(password, "UTF-8")
    return when (item.streamType) {
        "live" -> "$base/live/$user/$pass/${item.id}.m3u8"
        "movie" -> {
            val ext = item.containerExtension.ifBlank { "mp4" }
            "$base/movie/$user/$pass/${item.id}.$ext"
        }
        "episode" -> {
            val ext = item.containerExtension.ifBlank { "mp4" }
            "$base/series/$user/$pass/${item.id}.$ext"
        }
        else -> when (section) {
            "TV AO VIVO" -> "$base/live/$user/$pass/${item.id}.m3u8"
            "FILMES" -> "$base/movie/$user/$pass/${item.id}.${item.containerExtension.ifBlank { "mp4" }}"
            else -> "$base/series/$user/$pass/${item.id}.${item.containerExtension.ifBlank { "mp4" }}"
        }
    }
}
