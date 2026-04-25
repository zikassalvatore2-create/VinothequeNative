const fs = require('fs');
const path = require('path');

const rootDir = "C:\\Users\\zakar\\Downloads\\VinothequeNative";
const pkgDir = path.join(rootDir, 'app', 'src', 'main', 'java', 'com', 'vinotheque', 'nativeapp');

function mkdirP(dir) {
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
}

// 1. Update app/build.gradle.kts to add Room and Navigation
const appBuildGradle = path.join(rootDir, 'app', 'build.gradle.kts');
let gradleContent = fs.readFileSync(appBuildGradle, 'utf8');

if (!gradleContent.includes('kotlin-kapt')) {
    gradleContent = gradleContent.replace(
        'id("org.jetbrains.kotlin.android")',
        'id("org.jetbrains.kotlin.android")\n    id("kotlin-kapt")'
    );
}

if (!gradleContent.includes('androidx.room')) {
    const deps = `
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Lifecycle ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
`;
    gradleContent = gradleContent.replace('dependencies {', 'dependencies {' + deps);
    fs.writeFileSync(appBuildGradle, gradleContent);
}

// 2. Wine.kt (Entity)
const wineKt = `package com.vinotheque.nativeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wines")
data class Wine(
    @PrimaryKey val reference: String,
    val name: String,
    val region: String,
    val vintage: String,
    val grape: String,
    val type: String,
    val price: Double,
    val rating: Int,
    val peakMaturity: String,
    val binLocation: String,
    val body: Int,
    val tannin: Int,
    val acidity: Int,
    val sweetness: Int
)
`;
mkdirP(path.join(pkgDir, 'data'));
fs.writeFileSync(path.join(pkgDir, 'data', 'Wine.kt'), wineKt);

// 3. WineDao.kt
const wineDaoKt = `package com.vinotheque.nativeapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WineDao {
    @Query("SELECT * FROM wines ORDER BY name ASC")
    fun getAllWines(): Flow<List<Wine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWine(wine: Wine)

    @Query("DELETE FROM wines WHERE reference = :ref")
    suspend fun deleteWine(ref: String)
}
`;
fs.writeFileSync(path.join(pkgDir, 'data', 'WineDao.kt'), wineDaoKt);

// 4. AppDatabase.kt
const dbKt = `package com.vinotheque.nativeapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Wine::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wineDao(): WineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vinotheque_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
`;
fs.writeFileSync(path.join(pkgDir, 'data', 'AppDatabase.kt'), dbKt);

// 5. WineViewModel.kt
const vmKt = `package com.vinotheque.nativeapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinotheque.nativeapp.data.AppDatabase
import com.vinotheque.nativeapp.data.Wine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WineViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).wineDao()

    val wines: StateFlow<List<Wine>> = dao.getAllWines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSampleWine() {
        viewModelScope.launch {
            dao.insertWine(
                Wine(
                    reference = "REF\${System.currentTimeMillis()}",
                    name = "Château Margaux",
                    region = "Margaux, Bordeaux",
                    vintage = "2015",
                    grape = "Cabernet Sauvignon",
                    type = "Red",
                    price = 650.0,
                    rating = 98,
                    peakMaturity = "2025-2040",
                    binLocation = "Rack A1",
                    body = 4,
                    tannin = 4,
                    acidity = 3,
                    sweetness = 1
                )
            )
        }
    }
}
`;
mkdirP(path.join(pkgDir, 'ui'));
fs.writeFileSync(path.join(pkgDir, 'ui', 'WineViewModel.kt'), vmKt);

// 6. CellarScreen.kt
const cellarKt = `package com.vinotheque.nativeapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.data.Wine

@Composable
fun CellarScreen(viewModel: WineViewModel) {
    val wines by viewModel.wines.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar()
        
        if (wines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { viewModel.addSampleWine() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFd4a54e))
                ) {
                    Text("Add Sample Wine", color = Color.Black)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(wines) { wine ->
                    WineCard(wine)
                }
            }
        }
    }
}

@Composable
fun TopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1a0a0a))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🍷 Vinothèque Pro", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WineCard(wine: Wine) {
    val goldBorder = Brush.linearGradient(listOf(Color(0xFFd4a54e), Color(0xFF9a7b3a)))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .border(1.dp, goldBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x332d1212))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(wine.name, color = Color(0xFFd4a54e), fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 2)
            Text(wine.region, color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(wine.vintage, color = Color.Gray, fontSize = 12.sp)
                Text(wine.type, color = Color.Gray, fontSize = 12.sp)
            }
            
            Divider(color = Color(0xFFd4a54e).copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("€\${wine.price.toInt()}", color = Color(0xFFd4a54e), fontWeight = FontWeight.Bold)
                Text("★ \${wine.rating}", color = Color(0xFFd4a54e))
            }
        }
    }
}
`;
fs.writeFileSync(path.join(pkgDir, 'ui', 'CellarScreen.kt'), cellarKt);

// 7. MainActivity.kt (Overwrite)
const mainKt = `package com.vinotheque.nativeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.vinotheque.nativeapp.ui.CellarScreen
import com.vinotheque.nativeapp.ui.WineViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = Color(0xFF0d0505)) {
                    VinothequeApp()
                }
            }
        }
    }
}

@Composable
fun VinothequeApp() {
    val navController = rememberNavController()
    val viewModel: WineViewModel = viewModel()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        containerColor = Color(0xFF0d0505),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1a0a0a),
                contentColor = Color(0xFFd4a54e)
            ) {
                NavigationBarItem(
                    icon = { Text("📊") },
                    label = { Text("Insights") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFd4a54e),
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFFd4a54e),
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0x33d4a54e)
                    )
                )
                NavigationBarItem(
                    icon = { Text("🍷") },
                    label = { Text("Cellar") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFd4a54e),
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFFd4a54e),
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0x33d4a54e)
                    )
                )
            }
        }
    ) { innerPadding ->
        if (selectedTab == 1) {
            Modifier.padding(innerPadding)
            CellarScreen(viewModel)
        } else {
            // Dashboard placeholder
            Text(
                "Dashboard Coming Soon",
                color = Color.White,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
`;
fs.writeFileSync(path.join(pkgDir, 'MainActivity.kt'), mainKt);

console.log("Phase 1 Kotlin files created!");
