package com.example.meerkatservice.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.meerkatservice.ui.theme.MeerkatServiceTheme

/**
 * ローディング中であることを示すシンプルなCompose関数。
 * 画面中央にCircularProgressIndicatorと"Loading..."というテキストを表示します。
 */
@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(), // 画面全体に表示
        verticalArrangement = Arrangement.Center, // 垂直方向に中央揃え
        horizontalAlignment = Alignment.CenterHorizontally // 水平方向に中央揃え
    ) {
        CircularProgressIndicator() // ローディングインジケーター
        Text(text = "Loading...")   // ローディングテキスト
    }
}

/**
 * LoadingScreen関数のプレビュー。
 * Android Studioのデザインビューでこのコンポーザブルの外観を確認できます。
 */
@Preview(showBackground = true) // showBackground = true でプレビューに背景を表示
@Composable
fun LoadingScreenPreview() {
    // プレビュー用にテーマを適用することもできます（オプション）
    MeerkatServiceTheme { // あなたのアプリのテーマに置き換えてください
        LoadingScreen()
    }
}
