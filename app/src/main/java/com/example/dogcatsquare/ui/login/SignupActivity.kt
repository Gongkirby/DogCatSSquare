package com.example.dogcatsquare.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ActivitySignupBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class SignupActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding

    private var nickname_check: Boolean = false
    private var email_check: Boolean = false
    private var email_verify_check: Boolean = false
    private var pw_check: Boolean = false
    private var checkbox_check: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // 배경화면 클릭 시 키보드 숨기기
        binding.signupCl.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN){
                currentFocus?.let { view ->
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    view.clearFocus()
                }
            }
            false // 터치 이벤트를 소비하지 않음
        }

        binding.nicknameCheckBtn.setOnClickListener {
            val nickname = binding.nicknameEt.text.toString()
            if (isNicknameUsed(nickname)) { // 이미 사용 중인 닉네임
                binding.signupNicknameCheckTv.text = "이미 사용 중인 닉네임입니다"
                binding.signupNicknameCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
            else {
                binding.signupNicknameCheckTv.text = "사용 가능한 닉네임입니다"
                binding.signupNicknameCheckTv.setTextColor(ContextCompat.getColor(this, R.color.main_color1))
                nickname_check = true
            }
        }

        binding.emailCheckBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            if (isEmailUsed(email)) { // 이미 사용 중인 이메일
                binding.signupEmailCheckTv.text = "이미 사용 중인 이메일입니다"
                binding.signupEmailCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
            else {
                binding.signupEmailCheckTv.text = "사용 가능한 이메일입니다"
                binding.signupEmailCheckTv.setTextColor(ContextCompat.getColor(this, R.color.main_color1))
//                email_check = true

                binding.textView41.visibility = View.VISIBLE
                binding.verifyEmailEt.visibility = View.VISIBLE
                binding.verifyEmailBtn.visibility = View.VISIBLE
            }
        }

        binding.verifyEmailBtn.setOnClickListener {
            verifyEmail()
        }

        setupValidation()

        // 다음 버튼 클릭 시 바텀 시트 뜸
        binding.signupNextBtn.setOnClickListener {
            showBottomSheetDialog()
        }
    }

    // 약관동의 바텀시트
    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)

        // 레이아웃 인플레이트
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_signup, null)
        bottomSheetDialog.setContentView(view)

        // 체크박스 초기화
        val checkBoxAll = view.findViewById<CheckBox>(R.id.checkBox_all)
        val checkBox1 = view.findViewById<CheckBox>(R.id.checkBox1)
        val checkBox2 = view.findViewById<CheckBox>(R.id.checkBox2)
        val checkBox3 = view.findViewById<CheckBox>(R.id.checkBox3)
        val doneButton = view.findViewById<Button>(R.id.signup_done_btn)

        // 버튼 초기 상태 비활성화
        updateButtonState(checkBox1, checkBox2, doneButton)

        // 약관 전체 동의 체크박스 클릭 시 전체 체크 박스 ON
        checkBoxAll.setOnCheckedChangeListener { _, isChecked ->
            checkBox1.isChecked = isChecked
            checkBox2.isChecked = isChecked
            checkBox3.isChecked = isChecked
            updateButtonState(checkBox1, checkBox2, doneButton)
        }

        // 각 개별 체크박스 클릭 리스너
        val individualCheckBoxes = listOf(checkBox1, checkBox2, checkBox3)
        individualCheckBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, _ ->
                // 전체 동의 체크박스 업데이트
                checkBoxAll.isChecked = individualCheckBoxes.all { it.isChecked }

                // 완료 버튼 활성화 상태 업데이트
                updateButtonState(checkBox1, checkBox2, doneButton)
            }
        }

        // 완료 버튼 클릭 이벤트
        doneButton.setOnClickListener {
            checkSignup()
        }

        // 바텀시트 다이얼로그 표시
        bottomSheetDialog.show()
    }

    // 버튼 상태 업데이트 함수
    private fun updateButtonState(checkBox1: CheckBox, checkBox2: CheckBox, doneButton: Button) {
        // 필수 체크박스 모두 체크되었는지 확인
        val isMandatoryChecked = checkBox1.isChecked && checkBox2.isChecked

        // 버튼 활성화/비활성화 상태 설정
        doneButton.isEnabled = isMandatoryChecked
        if (isMandatoryChecked) {
            doneButton.setBackgroundColor(ContextCompat.getColor(this, R.color.main_color1)) // 활성화 색상
            doneButton.setTextColor(ContextCompat.getColor(this, R.color.white)) // 텍스트 색상
            checkbox_check = true
        } else {
            doneButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray1)) // 비활성화 색상
            doneButton.setTextColor(ContextCompat.getColor(this, R.color.gray4)) // 텍스트 색상
        }
    }

    // 닉네임 체크
    private fun validateNickname() {
        val nickname = binding.nicknameEt.text.toString()
        val nicknameCheckTv = binding.signupNicknameCheckTv

        // 정규식: 한글, 영어 숫자 구성, 최대 10자
        val nicknameRegex = "^[a-zA-Zㄱ-힣0-9]{1,10}$".toRegex()

        if (!nickname.matches(nicknameRegex)) {
            nicknameCheckTv.text = "한글, 영어 최대 10자"
            nicknameCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
            binding.nicknameCheckBtn.isClickable = false
            nickname_check = false
        } else {
            nicknameCheckTv.text = ""
            nicknameCheckTv.setTextColor(ContextCompat.getColor(this, R.color.main_color1))
            binding.nicknameCheckBtn.isClickable = true
        }
    }

    // 닉네임 중복 체크 (임시 함수)
    private fun isNicknameUsed(nickname: String): Boolean {
        // 실제 구현에서는 서버와의 통신으로 중복 확인해야 함
        return nickname == "닉네임"
    }

    // 이메일 체크 -> 인증
    private fun validateEmail() {
        val email = binding.emailEt.text.toString()
        val emailCheckTv = binding.signupEmailCheckTv

        // 이메일 정규식
        val idRegex = "^[a-zA-Z0-9]{1,15}$".toRegex()

        if (!email.matches(idRegex)) {
            emailCheckTv.text = "유효하지 않은 아이디 형식입니다"
            emailCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
            binding.emailCheckBtn.isClickable = false
            email_check = false
        } else {
            emailCheckTv.text = ""
            emailCheckTv.setTextColor(ContextCompat.getColor(this, R.color.main_color1))
            binding.emailCheckBtn.isClickable = true
        }
    }

    // 이메일 중복 체크 (임시 함수)
    private fun isEmailUsed(email: String): Boolean {
        // 실제 구현에서는 서버와의 통신으로 중복 확인해야 함
        return email == "test1234@gmail.com"
    }

    // 이메일 인증
    private fun verifyEmail() {
        binding.emailCheckBtn.setOnClickListener {

        }
    }

    // 비밀번호 체크
    private fun validatePassword() {
        val password = binding.pwEt.text.toString()
        val passwordCheck = binding.pwCheckEt.text.toString()
        val passwordCheckTv = binding.signupPwCheckTv

        // 정규식: 소문자, 숫자 포함 8~15자
        val passwordRegex = "^(?=.*[a-z])(?=.*\\d)[a-zA-Z\\d]{8,15}$".toRegex()

        if (!password.matches(passwordRegex)) {
            passwordCheckTv.text = "소문자, 숫자 필수 포함 8~15자"
            passwordCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else if (password != passwordCheck) { // 비밀번호 확인 불일치
            passwordCheckTv.text = "비밀번호가 불일치합니다"
            passwordCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else {
            passwordCheckTv.text = "비밀번호가 일치합니다"
            passwordCheckTv.setTextColor(ContextCompat.getColor(this, R.color.main_color1))
            pw_check = true
        }
    }

    private fun setupValidation() {
        binding.nicknameEt.addTextChangedListener {
            validateNickname()
        }

        binding.emailEt.addTextChangedListener {
            validateEmail()
        }

        binding.pwEt.addTextChangedListener {
            validatePassword()
        }

        binding.pwCheckEt.addTextChangedListener {
            validatePassword()
        }
    }


    private fun checkSignup() {
        if (nickname_check && email_verify_check && pw_check && checkbox_check) {
            val intent = Intent(this, SignupPetInfoActivity::class.java)
            startActivity(intent)
        }
    }
}