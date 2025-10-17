package kr.ac.dmu.midexam

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import krac.dmu.studymid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // binding 변수 선언 및 지연 초기화
    val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    // 다비아스의 방향 변경에 따라 레이아웃을 다르게 적용하려면
    // onConfigurationChanged 함수에서 binding 변수의 값을 변경해야 하기 때문에
    // var로 binding 변수를 선언해야 한다.
    //lateinit var binding: ActivityMainBinding

    // registerForActivityResult 함수를 사용하는 방법으로 activity 이동을 위한 변수 선언
    lateinit var intentLauncher: ActivityResultLauncher<Intent>
    lateinit var secondActivity: Intent
    //

    // RadioButton의 선택 결과를 저장하는 변수와 CheckBox의 선택 결과를 저장하는 변수
    // RadioButton은 Group에서 하나만 선택되기 때문에 첫 번째 항목의 선택을 기본으로 하고, 초기값 1로 설정
    // CheckBox는 0개 이상이 선택될 수 있기 때문에 초기값을 0으로 설정
    var radioResult: Int = 1
    var checkResult: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_main)
        // 방향 설정하려면 setContentView 앞에 설정해야 함.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        // 또는 - 두 가지 문장 중 위의 것을 사용하는 것을 권장함. 아래 문장은 경고 메시지가 표시됨
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR)
        //
        // onConfigurationChanged 함수에서 binding 변수의 값을 변경해야 하기 때문에
        // var로 binding 변수를 선언하면 여기서 초기회해야 한다.
        binding = ActivityMainBinding.inflate(layoutInflater)
        //
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // toolbar 설정
        setSupportActionBar(binding.toolbar)  // toolbar를 ActionBar로 설정
        supportActionBar?.setDisplayShowTitleEnabled(false) // ActionBar에 기본으로 표시되는 앱 이름을 표시되지 않도록 설정
        binding.toolbar.setTitle("홍길동")        // 툴바의 제목으로 자신의 이름 설정
        //

        // SharedPreference 사용하기
        val pref = getSharedPreferences("pref", 0)  // 이름 "pref"로 SharedPreference 파일 만들기
        // 파일이 있으면 사용하고, 없으면 새로 만든다.
        val edit = pref.edit()       // 편집용(수정 가능한) SharedPreference 객체 가져오기
        edit.putString("fontsize", "15")   // 편집용 SharedPreference 객체에 데이터 저장하기
        // 해당 이름의 데이터가 있으면 덮어쓰기, 없으면 쓰기
        edit.apply()                    // 변경 사항 적용하기
        //

        // RadioButton 중 첫 번째 RadioButton을 기본으로 선택되어 있도록 한다.
        binding.radioButton1.isChecked = true
        //

        binding.button1.setOnClickListener() {
            Log.d("myCheck", "버튼이 클릭되었습니다.")
        }

        binding.button2.setOnClickListener( object: View.OnClickListener {
            override fun onClick(view: View) {
                Log.d("myCheck", "${view.id} 버튼이 클릭되었습니다.")
            }
        })

        binding.button3.setOnClickListener{ view -> {
            Log.d("myCheck", "${view.id} 버튼이 클릭되었습니다.")
        }}

        binding.button4.setOnClickListener() {
            Toast.makeText(this, "버튼이 클릭되었습니다.", Toast.LENGTH_SHORT).show()
        }

        binding.button5.setOnClickListener() {
            var intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }

        // 이동할 activity의 정보를 갖는 secondIntent 변수 초기화
        secondActivity = Intent(this, SecondActivity::class.java)

        // registerForActivityResult 함수를 사용하는 방법으로 activity 이동을 위한 변수 초기화
        intentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            var message = when(it.resultCode) {
                RESULT_OK ->  "확인"
                RESULT_CANCELED -> "취소"
                else -> "모름"
            }
            //Log.d("myCheck", "Activity 이동으로 돌아온 상태 확인 ${message}" )
            Toast.makeText(this, "돌아온 결과는 ${message}", Toast.LENGTH_SHORT).show()
        }

        // registerForActivityResult 함수를 사용하는 방법으로 activity 이동 버튼 클릭 이벤트 핸들러
        binding.button7.setOnClickListener() {
            intentLauncher.launch(secondActivity)
        }

        // RadioButton 클릭
        binding.radioButton1.setOnClickListener() {
            radioResult = 1
            showRadioResult()   // radioButton의 선택 결과를 보여주는 사용자 함수
        }
        binding.radioButton2.setOnClickListener() {
            radioResult = 2
            showRadioResult()
        }
        binding.radioButton3.setOnClickListener() {
            radioResult = 3
            showRadioResult()
        }

        // checkBox 클릭
        binding.checkBox1.setOnClickListener() {
            if(binding.checkBox1.isChecked) {
                // checkBox에 on
                checkResult += 1

            } else {
                // checkBox에 off
                checkResult -= 1
            }
            showCheckResult()   // checkBox의 선택 결과를 보여주는 사용자 함수
        }
        binding.checkBox2.setOnClickListener() {
            if(binding.checkBox2.isChecked) {
                // checkBox에 on
                checkResult += 2

            } else {
                // checkBox에 off
                checkResult -= 2
            }

            showCheckResult()
        }



    } // onCreate

    // radioButton의 선택 결과를 보여주는 사용자 함수
    fun showRadioResult() {
        when(radioResult) {
            1 -> Log.d("myCheck", "${R.id.radioButton1}이 클릭되었습니다.")
            2 -> Log.d("myCheck", "${R.id.radioButton2}이 클릭되었습니다.")
            else -> Log.d("myCheck", "${R.id.radioButton3}이 클릭되었습니다.")
        }
    }

    // checkBox의 선택 결과를 보여주는 사용자 함수
    fun showCheckResult() {
        when(checkResult) {
            0 -> {
                // 아무 것도 선택되어 있지 않은 상태
                Log.d("myCheck", "아무것도 선택되지 않았습니다.")
            }
            1 -> {
                // 첫 번째 checkbox만 선택되어 있는 상태
                Log.d("myCheck", "${R.id.checkBox1} checkbox가 선택되었습니다.")
            }
            2 -> {
                // 두 번째 checkbox만 선택되어 있는 상태
                Log.d("myCheck", "${R.id.checkBox2} checkbox가 선택되었습니다.")
            }
            3 -> {
                // 두 가지 checkbox 모두 선택되어 있는 상태
                Log.d("myCheck", "${R.id.checkBox1}과 ${R.id.checkBox2} checkbox가 모두 선택되었습니다.")
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 디바이스 방향이 변경되면, 레이아웃 파일을 다시 읽어 binding 변수를 설정해야 한다.
        binding = ActivityMainBinding.inflate(layoutInflater)
        // binding 변수의 값이 변경되면, setContentView() 함수도 다시 실행해야 한다.
        setContentView(binding.root)
        // 이렇게 하려면 portrait/landscape 레이아웃이 모두 정의되어 있어야 한다.
    }

    // res -> menu -> toolbar_menu.xml 파일을 툴바의 메뉴로 설정하기
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // 툴바에 설정된 메뉴 항목을 선택할 때 이벤트 핸들러
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.item1 -> {
                Toast.makeText(this, "메뉴 item1이 선택되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}