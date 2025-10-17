package krac.dmu.studymid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kr.ac.dmu.midexam.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        // viewBinding을 위한 변수 선언
        lateinit var binding: ActivitySecondBinding

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_second)
        // viewBinding을 위한 변수 초기화와 setContentView() 함수 실행
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // SharedPreference 객체로부터 데이터 가져오기
        val pref = getSharedPreferences("pref", 0)
        val fontsize = pref.getString("fontsize", "14")
        Toast.makeText(this, "SharedPreference에서 가져온 fontsize는 ${fontsize}", Toast.LENGTH_SHORT).show()

        binding.button6.setOnClickListener() {
            finish()   // MainActivity로 돌아가기
        }

        binding.button8.setOnClickListener() {
            // RESULT_OK로 돌아가기
            setResult(RESULT_OK)
            finish()
        }

        binding.button9.setOnClickListener() {
            // RESULT_CANCELED로 돌아가기
            setResult(RESULT_CANCELED)
            finish()

        }
    } // onCreate
}