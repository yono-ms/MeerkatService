package com.example.meerkatservice.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meerkatservice.extensions.oldLocationScreenFlow
import com.example.meerkatservice.extensions.saveOldLocationScreen
import com.example.meerkatservice.logger
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme
import kotlinx.coroutines.launch

/**
 * アプリのメイン画面を表すシンプルなCompose関数。
 * "ようこそ！"というテキストと、クリック可能なボタンを表示します。
 */
@Composable
fun MainScreen(onButtonClicked: () -> Unit = {}) { // ボタンクリック時のアクションを受け取るラムダ
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val oldLocationScreen by context.oldLocationScreenFlow.collectAsState(false)
    Column(
        modifier = Modifier
            .fillMaxSize() // 画面全体に表示
            .padding(16.dp), // 全体にパディングを追加
        verticalArrangement = Arrangement.Center, // 垂直方向に中央揃え
        horizontalAlignment = Alignment.CenterHorizontally // 水平方向に中央揃え
    ) {
        Text(
            text = "ようこそ！",
            style = MaterialTheme.typography.headlineMedium // マテリアルテーマのスタイルを適用
        )
        Spacer(modifier = Modifier.height(16.dp)) // テキストとボタンの間にスペースを追加
        Button(onClick = onButtonClicked) {
            Text("ここをクリック")
        }
        Spacer(modifier = Modifier.height(16.dp)) // テキストとボタンの間にスペースを追加
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Old Location Screen")
            Spacer(modifier = Modifier.weight(1F))
            Switch(checked = oldLocationScreen, onCheckedChange = {
                scope.launch {
                    runCatching {
                        context.saveOldLocationScreen(it)
                    }.onFailure {
                        logger.error("saveOldLocationScreen", it)
                    }
                }
            })
        }
    }
}

/**
 * MainScreen関数のプレビュー。
 * Android Studioのデザインビューでこのコンポーザブルの外観を確認できます。
 */
@Preview(showBackground = true, name = "MainScreen Preview") // プレビューに名前を付けることも可能
@Composable
fun MainScreenPreview() {
    // プレビュー用にテーマを適用することが推奨されます
    MeerkatServiceTheme { // あなたのアプリのテーマに置き換えてください
        MainScreen(onButtonClicked = {
            // プレビューではボタンクリック時の具体的な動作は不要な場合が多い
            println("Button clicked in Preview")
        })
    }
}