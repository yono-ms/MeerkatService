package com.example.meerkatservice.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme

/**
 * アプリのメイン画面を表すシンプルなCompose関数。
 * "ようこそ！"というテキストと、クリック可能なボタンを表示します。
 */
@Composable
fun MainScreen(onButtonClicked: () -> Unit = {}) { // ボタンクリック時のアクションを受け取るラムダ
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