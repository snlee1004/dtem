function checkWrite() {
	var frm = document.frmWrite;

	if (!frm.name.value) {
		alert("이름을 입력하세요.");
		frm.name.focus();
		return false;
	}
	if (!frm.id.value) {
		alert("아이디를 입력하세요.");
		frm.id.focus();
		return false;
	}
	if (!frm.pwd.value) {
		alert("비밀번호를 입력하세요.");
		frm.pwd.focus();
		return false;
	}
	if (frm.pwd.value != frm.repwd.value) {
		alert("비밀번호가 맞지 않습니다.");
		return false;
	}
	// 데이터 전송
	frm.submit();
}

// 로그인 화면 입력 검사
function checkLogin() {
	var frm = document.frm;

	if (!frm.id.value) {
		alert("이메일을 입력하세요.");
		frm.id.focus();
		return false;
	}
	if (!frm.pwd.value) {
		alert("비밀번호를 입력하세요.");
		frm.pwd.focus();
		return false;
	}

	frm.submit();
}

// 아이디 중복 검사창 띄우기
function checkId() {	
	var sId = document.frmWrite.id.value;  // id 입력태그 값 읽어오기
	if (!sId) {
		alert("아이디를 입력하세요.");
		document.frmWrite.id.focus();
		return false;
	} else {
		window.open("../member/checkId.do?id=" + sId, "",
			"width=600 height=200 left=500 top=200")
	}
}

function checkModify() {
	var frm = document.frm;

	if (!frm.name.value) {
		alert("이름을 입력하세요.");
		frm.name.focus();
		return false;
	}	
	if (!frm.pwd.value) {
		alert("비밀번호를 입력하세요.");
		frm.pwd.focus();
		return false;
	}
	if (frm.pwd.value != frm.repwd.value) {
		alert("비밀번호가 맞지 않습니다.");
		return false;
	}
	// 데이터 전송
	frm.submit();
}




