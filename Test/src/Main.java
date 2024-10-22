// package PL112_11027215;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Stack;
import java.util.function.DoubleUnaryOperator;

//class Main {
//  public static void main( String[] args ) {
//    String str = "1+2/2";
//  }
//
//  public static void toRPN( String str ) {
//
//
//
//
//
//
//  }
//
//  public static int get_weight( char ch ) {
//    if ( ch == '*' || ch == '/' ) return 10;
//    if ( ch == '+' || ch == '-' ) return 5;
//    return 0;
//  }
//}

class Token{
// only ID may have value
// 4.3 is a NUM, name is 4.3

  String m_name;
  String m_value;
  String m_type;
  String m_def_ins;

  public void Set_name( String str ) {
    this.m_name = str;
  } // Set_name()

  public void Set_value( String str ) {
    this.m_value = str;
  } // Set_value()

  public void Set_type( String str ) {
    this.m_type = str;
  } // Set_type()

  public void  Set_def_ins( String str ) {
    this.m_def_ins = str;
  } // Set_def_ins()

  public String Get_name() {
    return m_name;
  } // Get_name()

  public String Get_value() {
    return m_value;
  } // Get_value()

  public String Get_type() {
    return m_type;
  } // Get_type()

  public String Get_def_ins() {
    return m_def_ins;
  } // Get_def_ins()

} // class Token

class Reader{

  String m_current_line;
  int m_current_pos; // current char position (index)
  // 用下面的變數來處理輸出error line
  int m_num_of_line;
  int m_last_tk_line;
  int m_last_correct_line;
  int m_first_line_of_zhujie; // 註解起始行數
  boolean m_in_zhujie;
  Scanner m_sc;
  ArrayList<String> m_history_line; // 歷史紀錄

  Reader( Scanner sc ) {
    m_current_line = null;
    m_current_pos = 0;
    m_num_of_line = 0;
    m_last_tk_line = 0;
    m_last_correct_line = 0;
    m_first_line_of_zhujie = 0;
    this.m_sc = sc;
    m_history_line = new ArrayList<String>();
  } // Reader()

  private String Classify_type( String str ) {
    // save words or special words
    char ch0 = str.charAt( 0 );

    if ( str.equals( "int" ) ) return "INT";

    else if ( str.equals( "float" ) ) return "FLOAT";

    else if ( str.equals( "char" ) ) return "CHAR";

    else if ( str.equals( "bool" ) ) return "BOOL";

    else if ( str.equals( "string" ) ) return "STRING"; // 全部小寫才是宣告

    else if ( str.equals( "void" ) ) return "VOID";

    else if ( str.equals( "if" ) ) return "IF";

    else if ( str.equals( "else" ) ) return "ELSE";

    else if ( str.equals( "while" ) ) return "WHILE";

    else if ( str.equals( "do" ) ) return "DO";

    else if ( str.equals( "return" ) ) return "RETURN";

    else if ( str.equals( ">=" ) ) return "GE";

    else if ( str.equals( "<=" ) ) return "LE";

    else if ( str.equals( "==" ) ) return "EQ";

    else if ( str.equals( "!=" ) ) return "NEQ";

    else if ( str.equals( "&&" ) ) return "AND";

    else if ( str.equals( "||" ) ) return "OR";

    else if ( str.equals( "+=" ) ) return "PE";

    else if ( str.equals( "-=" ) ) return "ME";

    else if ( str.equals( "*=" ) ) return "TE";

    else if ( str.equals( "/=" ) ) return "DE";

    else if ( str.equals( "%=" ) ) return "RE";

    else if ( str.equals( "++" ) ) return "PP";

    else if ( str.equals( "--" ) ) return "MM";

    else if ( str.equals( ">>" ) ) return "RS";

    else if ( str.equals( "<<" ) ) return "LS";

    else if ( str.equals( "true" ) || str.equals( "false" ) ) return "CONST"; // 這是CONST !!

    else if ( ch0 == '\'' || ch0 == '\"' ) return "CONST"; // 括號括起來的，"Hello" 'c'等等

    else if ( Character.isDigit( ch0 ) || ( str.length() != 1 && ch0 == '.' ) ) return "CONST"; // 數字或是小數點開頭

    else if ( Is_other( ch0 ) ) return "OTHERS";

    else if ( Is_letter( ch0 ) ) return "IDENT";

    else if ( ch0 == '\"' || ch0 == '\'' ) return "CONST";

    else return "UNKNOWN";
  } // Classify_type()

  private void Look_back() {
    if ( m_current_pos > 0 ) {
      m_current_pos = m_current_pos - 1;
    } // if
  } // Look_back()

  private boolean Is_letter( char ch ) {

    if ( ch >= 'a' && ch <= 'z' ) {
      return true;
    } // if

    else if ( ch >= 'A' && ch <= 'Z' ) {
      return true;
    } // else if

    return false;
  } // Is_letter()

  private boolean Is_other( char ch ) {

    if ( ch == '(' || ch == ')' || ch == '[' || ch == ']' ) return true;
    else if ( ch == '{' || ch == '}' || ch == '+' || ch == '-' ) return true;
    else if ( ch == '*' || ch == '/' || ch == '%' || ch == '^' ) return true;
    else if ( ch == '>' || ch == '<' || ch == '&' || ch == '|' ) return true;
    else if ( ch == '=' || ch == '!' || ch == ';' || ch == ',' ) return true;
    else if ( ch == '?' || ch == ':' ) return true;
    return false;
  } // Is_other()

  private char Skip_space() {
    // return the char following by space
    char ch = Read_char();

    while ( ch == ' ' ) {
      ch = Read_char();
    } // while()

    return ch;
  } // Skip_space()

  private char Read_char() {

    if ( m_current_pos < m_current_line.length() ) {
      char ch = m_current_line.charAt( m_current_pos );
      m_current_pos = m_current_pos + 1;
      return ch;
    } // if

    return '\0';
  } // Read_char()

  public void Skip_line() {
    m_current_pos = m_current_line.length();
  } // Skip_line()

  private String Read_digit( boolean find_dot ) {
    // return the following digit
    String ret = "";
    char ch = Read_char();

    if ( find_dot ) {
      // read until not digit
      while ( Character.isDigit( ch ) ) {
        ret = ret + ch;
        ch = Read_char();
      } // while
    } // if

    else {

      while ( Character.isDigit( ch ) ) {
        ret = ret + ch;
        ch = Read_char();
      } // while

      if ( ch == '.' ) {
        // second check
        ret = ret + ch;
        ch = Read_char();

        while ( Character.isDigit( ch ) ) {
          ret = ret + ch;
          ch = Read_char();
        } // while
      } // if
    } // else

    if ( ch != '\0' ) {
      // stop by different type
      Look_back();
    } // if

    return ret;
  } // Read_digit()

  private String Read_letter() {

    String ret = "";
    char ch = Read_char();

    while ( Is_letter( ch ) || Character.isDigit( ch ) || ch == '_' ) {
      ret = ret + ch;
      ch = Read_char();
    } // while

    if ( ch != '\0' ) {
      // stop by different type
      Look_back();
    } // if

    return ret;
  } // Read_letter()

  public Token Get_token() {

    m_last_tk_line = m_num_of_line; // 紀錄上一個token的行數，寫在最前面看之後讀了幾個換行
    Token ret = new Token();
    String str = "";

    if ( m_current_line == null ) {
      // first call
      m_current_line = m_sc.nextLine();
      m_current_pos = 0;
      m_num_of_line = m_num_of_line + 1;
      m_history_line.add( m_current_line );
    } // if

    char ch = Read_char();

    if ( ch == ' ' ) {
      ch = Skip_space();
    } // if

    while ( ch == '\0' && m_sc.hasNext() ) {
      // read new line
      // 換行
      m_current_line = m_sc.nextLine();
      m_current_pos = 0;
      m_num_of_line = m_num_of_line + 1;
      m_history_line.add( m_current_line );

      ch = Read_char();

      if ( ch == ' ' ) {
        ch = Skip_space();
      } // if
    } // while

    // ch is useful now
    str = str + ch;

    if ( Is_letter( ch ) ) {
      str = str + Read_letter();
    } // if

    else if ( Character.isDigit( ch ) ) {
      str = str + Read_digit( false );
    } // else if

    else if ( ch == '.' ) {
      // .123 is a digit
      str = str + Read_digit( true );
    } // else if

    else if ( Is_other( ch ) ) {
      // ++ 是 PP, -- 是 MM, 1+ +1 的 + 必定分開
      char next_ch = Read_char();
      boolean used = false; // if not used, look_back()

      if ( ch == '=' || ch == '>' || ch == '<' ) {
        // 可能是 >=, <=, ==, >>, <<, or single '>' '=' '<'
        if ( ch == '>' && next_ch == '=' ) {
          str = str + next_ch; // ">="
          used = true;
        } // if

        else if ( ch == '<' && next_ch == '=' ) {
          str = str + next_ch; // "<="
          used = true;
        } // else if

        else if ( ch == '=' && next_ch == '=' ) {
          str = str + next_ch;
          used = true;
        } // else if

        else if ( ch == '>' && next_ch == '>' ) {
          str = str + next_ch;
          used = true;
        } // else if

        else if ( ch == '<' && next_ch == '<' ) {
          str = str + next_ch;
          used = true;
        } // else if
      } // if

      else if ( ch == '!' && next_ch == '=' ) {
        // 可能是 !=
        str = str + next_ch;
        used = true;
      } // else if

      else if ( ch == '&' && next_ch == '&' ) {
        // 可能是 &&
        str = str + next_ch;
        used = true;
      } // else if

      else if ( ch == '|' && next_ch == '|' ) {
        // 可能是 ||
        str = str + next_ch;
        used = true;
      } // else if

      else if ( ch == '+' && next_ch == '+' ) {
        str = str + next_ch;
        used = true;
      } // else if

      else if ( ch == '-' && next_ch == '-' ) {
        str = str + next_ch;
        used = true;
      } // else if

      else if ( ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%' ) {
        if ( next_ch == '=' ) {
          str = str + next_ch;
          used = true;
        } // if
      } // else if

      if ( ch == '/' && next_ch == '/' ) {
        // comment
        Skip_line();

        if ( Whole_line_zhujie( m_current_line ) && !m_in_zhujie ) {
          m_first_line_of_zhujie = m_num_of_line;
          m_in_zhujie = true;
        } // if

        return Get_token();
      } // if

      else if ( next_ch != '\0' && !used ) {
        Look_back();
      } // else if

    } // else if

    else if ( ch == '\"' ) {
      // 讀取直到遇到下一個 "
      do {
        ch = Read_char();
        while ( ch == '\0' ) {
          // 換行
          m_current_line = m_sc.nextLine();
          m_current_pos = 0;
          m_num_of_line = m_num_of_line + 1;
          m_history_line.add( m_current_line );
          ch = Read_char();
        } // while

        str = str + ch;
      } while ( ch != '\"' ); // do while
    } // else if

    else if ( ch == '\'' ) {
      // 讀一個char
      ch = Read_char();
      str = str + ch;
      ch = Read_char();
      str = str + ch;
    } // else if

    String type = Classify_type( str );
    ret.Set_name( str );
    ret.Set_value( "" );
    ret.Set_type( type );
    ret.Set_def_ins( "" );
    return ret;
  } // Get_token()

  public boolean Has_next_tk() {
    // 檢查m_current_pos到m_current_line結束前是否還有token
    char ch = Skip_space();
    if ( ch == '\0' ) return false;

    if ( ch == '/' ) {
      char next_ch = Read_char();
      if ( next_ch == '/' ) {
        Skip_line();
        return false;
      } // if

      Look_back(); // 多讀了一個next_ch
    } // if

    Look_back(); // 多讀了一個ch
    return true;
  } // Has_next_tk()

  private boolean Whole_line_zhujie( String line ) {
    // 檢查是否整行都是註解
    char ch, next_ch;
    for ( int i = 0; i < line.length() - 1 ; i++ ) {
      ch = line.charAt( i );
      next_ch = line.charAt( i+1 );
      if ( ch == '/' && next_ch == '/' ) return true;
      else if ( Character.isLetter( ch ) || Character.isDigit( ch ) ) return false;
      else if ( Is_other( ch ) ) return false;
    } // for

    return false;
  } // Whole_line_zhujie()

  private boolean Is_empty_line( String line ) {

    int idx = 0;
    while ( idx < line.length() ) {
      char ch = line.charAt( idx );
      if ( Character.isLetter( ch ) ) return false;
      if ( Character.isDigit( ch ) ) return false;
      if ( Is_other( ch ) ) return false;
      idx = idx + 1;
    } // while

    return true;
  } // Is_empty_line()

  public int Get_error_line() {
    int ret = m_num_of_line - m_last_correct_line;
    int idx = m_last_correct_line - 1;
    if ( idx > 0 ) {
      String line = m_history_line.get( idx );
      if ( Whole_line_zhujie( line ) ) {
        while ( idx > 0 && ( Whole_line_zhujie( line ) || Is_empty_line( line ) ) ) {
          // 回朔
          ret = ret + 1;
          idx = idx - 1;
          line = m_history_line.get( idx );
        } // while
      } // if
    } // if

    return ret;
  } // Get_error_line()

} // class Reader()

class Parser{
  // error priority: Unrecognized > Unexpected > Undefined
  Reader m_reader;
  Token m_curr_token;
  ArrayList<Token> m_instruction;
  Token m_unrecognized_tk; // token type = unknown
  Token m_unexpected_tk;   // syntax error token
  Token m_undefined_tk;    // token not in ID_table and not defined
  ArrayList<Token> m_ID_table;   // 紀錄已定義IDENT的table
  ArrayList<Token> m_func_table; // 紀錄已定義function的table
  Stack<Integer> m_tmpstack;     // compound statement可能會重複呼叫，所以不能用bool紀錄，改用stack
  ArrayList<Token> m_tmp_ID_table; // 假執行暫時定義的table
  boolean m_is_func_def;           // 記錄這個指令是不是function定義

  Parser( Reader r, ArrayList<Token> table1, ArrayList<Token> table2 ) {
    m_reader = r;
    m_instruction = new ArrayList<Token>();
    m_unrecognized_tk = null;
    m_unexpected_tk = null;
    m_undefined_tk = null;
    m_ID_table = table1;
    m_func_table = table2;
    m_tmpstack = new Stack<Integer>();
    m_tmp_ID_table = new ArrayList<Token>();
    m_is_func_def = false;
  } // Parser()

  private void Next_token() {

    m_curr_token = m_reader.Get_token();
    if ( m_curr_token != null && m_curr_token.Get_type().equals( "UNKNOWN" ) ) {
      m_unrecognized_tk = m_curr_token;
    } // if

    m_instruction.add( m_curr_token );
  } // Next_token()

  public void Reset_variable() {
    m_unrecognized_tk = null;
    m_unexpected_tk = null;
    m_undefined_tk = null;
    m_curr_token = null;
    m_tmp_ID_table.clear(); // 這個很重要
    m_is_func_def = false;
  } // Reset_variable()

  public void Clear_instruction() {
    m_instruction.clear();
  } // Clear_instruction()

  public ArrayList<Token> Get_instruction() {
    return m_instruction;
  } // Get_instruction()

  public void Give_back() {

    Clear_instruction();
    m_instruction.add( m_curr_token ); // 上一次解析多抓了一個token
  } // Give_back()

  public boolean First_is_Done() {
    // if first token is Done, return true
    if ( m_instruction.isEmpty() ) return false;

    else {
      for ( int i = 0; i < m_instruction.size() ; i++ ) {
        String n = m_instruction.get( i ).Get_name();
        if ( n.equals( "Done" ) ) return true;
      } // for
    } // else

    return false;
  } // First_is_Done()

  // support function
  private boolean Is_Definition_or_Statement( Token tk ) {
    String name = tk.Get_name();
    String type = tk.Get_type();

    if ( type.equals( "VOID" ) || IsTypeSpecifier( tk ) )
      return true;

    else if ( name.equals( ";" ) || type.equals( "RETURN" ) || type.equals( "IF" ) )
      return true;

    else if ( type.equals( "WHILE" ) || type.equals( "DO" ) )
      return true;

    else if ( type.equals( "IDENT" ) || IsPPorMM( tk ) || IsSign_2( tk ) )
      return true;

    else if ( type.equals( "CONST" ) || name.equals( "(" ) || name.equals( "{" ) )
      return true;

    return false;
  } // Is_Definition_or_Statement()

  private boolean Is_Declaration_or_Statement( Token tk ) {

    String name = tk.Get_name();
    String type = tk.Get_type();

    if ( IsTypeSpecifier( tk ) )
      return true; // Declaration

    else if ( name.equals( ";" ) || type.equals( "RETURN" ) || type.equals( "IF" ) )
      return true; // Statement

    else if ( type.equals( "WHILE" ) || type.equals( "DO" ) )
      return true; // Statement

    else if ( type.equals( "IDENT" ) || IsPPorMM( tk ) || IsSign_2( tk ) )
      return true; // Expression

    else if ( type.equals( "CONST" ) || name.equals( "(" ) || name.equals( "{" ) )
      return true; // Expression

    return false;
  } // Is_Declaration_or_Statement()

  private boolean IsTypeSpecifier( Token token ) {

    if ( token.m_type.equals( "INT" ) ) return true;
    else if ( token.m_type.equals( "CHAR" ) ) return true;
    else if ( token.m_type.equals( "FLOAT" ) ) return true;
    else if ( token.m_type.equals( "STRING" ) ) return true;
    else if ( token.m_type.equals( "BOOL" ) ) return true;
    else return false;
  } // IsTypeSpecifier()

  private boolean IsExpressionStart( Token token ) {

    if ( token.m_type.equals( "IDENT" ) || token.m_type.equals( "CONST" ) ) return true;
    else if ( IsPPorMM( token ) ) return true;
    else if ( IsSign_2( token ) ) return true;
    else if ( token.m_name.equals( "(" ) ) return true;
    return false;
  } // IsExpressionStart()

  private boolean IsAssignmentOperator( Token token ) {
    // 名字短一點，不然下面if長度會炸開
    String n = token.Get_name(); // name
    String t = token.Get_type(); // type

    if ( n.equals( "=" ) || t.equals( "TE" ) || t.equals( "DE" ) ) return true;
    else if ( t.equals( "RE" ) || t.equals( "PE" ) || t.equals( "ME" ) ) return true;
    else return false;
  } // IsAssignmentOperator()

  private boolean IsEqualityOperator( Token token ) {
    return token.m_type.equals( "EQ" ) || token.m_type.equals( "NEQ" );
  } // IsEqualityOperator()

  private boolean IsRelationalOperator( Token token ) {

    String n = token.Get_name();
    String t = token.Get_type();

    if ( n.equals( "<" ) || n.equals( ">" ) ) return true;
    else if ( t.equals( "LE" ) || t.equals( "GE" ) ) return true;
    else return false;
  } // IsRelationalOperator()

  private boolean IsMultiplicativeOperator( Token token ) {

    String n = token.Get_name();

    if ( n.equals( "*" ) || n.equals( "/" ) || n.equals( "%" ) ) return true;
    else return false;
  } // IsMultiplicativeOperator()

  private boolean IsShiftOperator( Token token ) {
    return token.Get_type().equals( "LS" ) || token.Get_type().equals( "RS" );
  } // IsShiftOperator()

  private boolean IsSign( Token token ) {
    return token.m_name.equals( "+" ) || token.m_name.equals( "-" );
  } // IsSign()

  private boolean IsSign_2( Token token ) {
    return token.m_name.equals( "+" ) || token.m_name.equals( "-" ) || token.m_name.equals( "!" );
  } // IsSign_2()

  private boolean IsPPorMM( Token token ) {
    return token.m_name.equals( "++" ) || token.m_name.equals( "--" );
  } // IsPPorMM()

  private boolean In_table( Token tk ) {
    String name = tk.Get_name();
    for ( int i = 0; i < m_ID_table.size() ; i++ ) {
      if ( m_ID_table.get( i ).Get_name().equals( name ) ) return true;
    } // for

    for ( int i = 0; i < m_func_table.size() ; i++ ) {
      if ( m_func_table.get( i ).Get_name().equals( name ) ) return true;
    } // for

    if ( Is_save_word( tk ) ) return true; // save word

    if ( !m_tmpstack.empty() ) {
      // 檢查在compound statement裡面的宣告
      for ( int i = 0; i < m_tmp_ID_table.size() ; i++ ) {
        if ( m_tmp_ID_table.get( i ).Get_name().equals( name ) ) return true;
      } // for
    } // if

    return false;
  } // In_table()

  private boolean Is_save_word( Token tk ) {
    // 特定名字的IDENT是保留字
    String n = tk.Get_name();
    if ( n.equals( "cin" ) || n.equals( "cout" ) ) return true;
    else if ( n.equals( "if" ) || n.equals( "else" ) ) return true;
    else if ( n.equals( "while" ) || n.equals( "do" ) ) return true;
    else if ( n.equals( "ListAllVariables" ) || n.equals( "ListVariable" ) ) return true;
    else if ( n.equals( "ListAllFunctions" ) || n.equals( "ListFunction" ) ) return true;
    return false;
  } // Is_save_word()

  // Parse function
  public boolean Parse_User_input() {
    // ( definition | statement ) { definition | statement }
    if ( m_curr_token == null ) Next_token();

    if ( m_curr_token != null ) {

      if ( m_curr_token.Get_name().equals( "Done" ) ) return true;

      if ( Is_Definition_or_Statement( m_curr_token ) ) {
        if ( IsTypeSpecifier( m_curr_token ) || m_curr_token.Get_type().equals( "VOID" ) ) {
          if ( !Parse_Definition() ) {
            if ( m_undefined_tk == null )
              m_unexpected_tk = m_curr_token;
            return false;
          } // if
        } // if

        else {
          if ( !Parse_Statement() ) {
            if ( m_undefined_tk == null )
              m_unexpected_tk = m_curr_token;
            return false;
          } // if
        } // else
      } // if

      else {
        m_unexpected_tk = m_curr_token;
        return false;
      } // else
    } // if

    return true;
  } // Parse_User_input()

  private boolean Parse_Definition() {
    //  VOID Identifier function_definition_without_ID
    //   | type_specifier Identifier function_definition_or_declarators
    //  這裡的IDENT不用檢查是否定義
    if ( m_curr_token.Get_type().equals( "VOID" ) ) {
      // 定義 function
      Next_token(); // eat VOID
      if ( !m_curr_token.Get_type().equals( "IDENT" ) ) {
        return false;
      } // if

      Next_token(); // eat IDENT
      return Parse_Function_definition_without_ID();
    } // if

    else if ( IsTypeSpecifier( m_curr_token ) ) {
      if ( !Parse_Type_specifier() ) {
        return false;
      } // if

      if ( !m_curr_token.Get_type().equals( "IDENT" ) ) {
        return false;
      } // if

      Next_token(); // eat IDENT
      return Parse_function_definition_or_declarators();
    } // else if

    else {
      return false;
    } // else
  } // Parse_Definition()

  private boolean Parse_Type_specifier() {
    // INT | CHAR | FLOAT | STRING | BOOL
    String tp = m_curr_token.Get_type();
    if ( tp.equals( "INT" ) || tp.equals( "CHAR" ) || tp.equals( "FLOAT" ) ) {
      Next_token();
      return true;
    } // if

    else if ( tp.equals( "STRING" ) || tp.equals( "BOOL" ) ) {
      Next_token();
      return true;
    } // else if

    else {
      return false;
    } // else
  } // Parse_Type_specifier()

  private boolean Parse_function_definition_or_declarators() {
    // function_definition_without_ID | rest_of_declarators
    if ( m_curr_token.Get_name().equals( "(" ) ) {
      if ( !Parse_Function_definition_without_ID() ) {
        return false;
      } // if
    } // if

    else {
      if ( !Parse_Rest_of_declarators() ) {
        return false;
      } // if
    } // else

    return true;
  } // Parse_function_definition_or_declarators()

  private boolean Parse_Rest_of_declarators() {
    // [ '[' Constant ']' ] { ',' Identifier [ '[' Constant ']' ] } ';'
    // 這裡的IDENT不用檢查是否定義
    if ( m_curr_token.Get_name().equals( "[" ) ) {
      Next_token(); // eat '['
      if ( !m_curr_token.Get_type().equals( "CONST" ) ) {
        return false;
      } // if

      Next_token(); // eat CONST
      if ( !m_curr_token.Get_name().equals( "]" ) ) {
        return false;
      } // if

      Next_token(); // eat ']'
    } // if

    while ( m_curr_token.Get_name().equals( "," ) ) {
      Next_token(); // eat ','
      if ( !m_curr_token.Get_type().equals( "IDENT" ) ) {
        return false;
      } // if

      if ( !m_tmpstack.empty() ) {
        // 是compound statement的呼叫
        m_tmp_ID_table.add( m_curr_token );
      } // if

      Next_token(); // eat IDENT
      if ( m_curr_token.Get_name().equals( "[" ) ) {
        Next_token(); // eat '['
        if ( !m_curr_token.Get_type().equals( "CONST" ) ) {
          return false;
        } // if

        Next_token(); // eat CONST
        if ( !m_curr_token.Get_name().equals( "]" ) ) {
          return false;
        } // if

        Next_token(); // eat ']'
      } // if
    } // while

    if ( !m_curr_token.Get_name().equals( ";" ) ) {
      return false;
    } // if

    Next_token(); // eat ';'
    return true;
  } // Parse_Rest_of_declarators()

  private boolean Parse_Function_definition_without_ID() {
    // '(' [ VOID | formal_parameter_list ] ')' compound_statement
    // 這裡的IDENT不用檢查是否定義
    // function定義
    m_is_func_def = true;
    if ( !m_curr_token.Get_name().equals( "(" ) ) {
      return false;
    } // if

    Next_token(); // eat '('
    if ( m_curr_token.Get_type().equals( "VOID" ) ) {
      Next_token(); // eat VOID
    } // if

    else if ( !m_curr_token.Get_name().equals( ")" ) ) {
      // 若不是')'代表可能有formal_parameter_list
      if ( !Parse_Formal_parameter_list() ) {
        return false;
      } // if
    } // else if

    if ( !m_curr_token.Get_name().equals( ")" ) ) {
      return false;
    } // if

    Next_token(); // eat ')'
    if ( !Parse_Compound_statement() ) {
      return false;
    } // if

    return true;
  } // Parse_Function_definition_without_ID()

  private boolean Parse_Formal_parameter_list() {
    // type_specifier [ '&' ] Identifier [ '[' Constant ']' ]
    //  { ',' type_specifier [ '&' ] Identifier [ '[' Constant ']' ] }
    // 有大量重複的部分，呼叫Parse_Parameter1()
    //  這裡的IDENT不用檢查是否定義
    if ( !Parse_Parameter() ) {
      return false;
    } // if

    while ( m_curr_token.Get_name().equals( "," ) ) {
      Next_token(); // eat ','
      if ( !Parse_Parameter() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Formal_parameter_list()

  private boolean Parse_Parameter() {
    // type_specifier [ '&' ] Identifier [ '[' Constant ']' ]
    // 這裡的IDENT不用檢查是否定義
    // 這裡可能是function的傳入參數
    if ( !Parse_Type_specifier() ) {
      return false;
    } // if

    if ( m_curr_token.Get_name().equals( "&" ) ) {
      Next_token(); // eat '&'
    } // if

    if ( !m_curr_token.Get_type().equals( "IDENT" ) ) {
      return false;
    } // if


    if ( m_is_func_def ) {
      // 假執行，讓後面compound statement的檢查可以過
      m_tmp_ID_table.add( m_curr_token );
    } // if

    Next_token(); // eat IDENT
    if ( m_curr_token.Get_name().equals( "[" ) ) {
      Next_token(); // eat '['
      if ( !m_curr_token.Get_type().equals( "CONST" ) ) {
        return false;
      } // if

      Next_token(); // eat CONST
      if ( !m_curr_token.Get_name().equals( "]" ) ) {
        return false;
      } // if

      Next_token(); // eat ']'
    } // if

    return true;
  } // Parse_Parameter()

  private boolean Parse_Compound_statement() {
    // '{' { declaration | statement } '}'
    Integer tmp = 1;
    m_tmpstack.push( tmp ); // 進來就push，離開就pop，這樣讓其他function知道現在是否在compound statement裡面
    if ( !m_curr_token.Get_name().equals( "{" ) ) {
      m_tmpstack.pop();
      return false;
    } // if

    Next_token(); // eat '{'
    while ( !m_curr_token.Get_name().equals( "}" ) ) {
      if ( m_curr_token == null ) {
        m_tmpstack.pop();
        return false;
      } // if

      if ( !Is_Declaration_or_Statement( m_curr_token ) ) {
        m_tmpstack.pop();
        return false;
      } // if

      if ( IsTypeSpecifier( m_curr_token ) ) {
        if ( !Parse_Declaration() ) {
          m_tmpstack.pop();
          return false;
        } // if
      } // if

      else {
        if ( !Parse_Statement() ) {
          m_tmpstack.pop();
          return false;
        } // if
      } // else

      // Parse_XXX會自動呼叫一次Next_token()
    } // while

    Next_token(); // eat '}'
    m_tmpstack.pop();
    return true;
  } // Parse_Compound_statement()

  private boolean Parse_Declaration() {
    // type_specifier Identifier rest_of_declarators
    //  這裡的IDENT不用檢查是否定義
    if ( !Parse_Type_specifier() ) {
      return false;
    } // if

    if ( !m_curr_token.Get_type().equals( "IDENT" ) ) {
      return false;
    } // if

    // 如果是parse_compound Statement的呼叫要丟入tmp table裡面假執行
    if ( !m_tmpstack.empty() ) {
      // 是compound statement的呼叫
      m_tmp_ID_table.add( m_curr_token );
    } // if

    Next_token(); // eat IDENT
    if ( !Parse_Rest_of_declarators() ) {
      return false;
    } // if

    return true;
  } // Parse_Declaration()

  private boolean Parse_Statement() {
    //   ';'
    //  | expression ';'
    //  | RETURN [ expression ] ';'
    //  | compound_statement
    //  | IF '(' expression ')' statement [ ELSE statement ]
    //  | WHILE '(' expression ')' statement
    //  | DO statement WHILE '(' expression ')' ';'
    if ( m_curr_token.Get_name().equals( ";" ) ) {
      Next_token(); // eat ';'
      return true;
    } // if

    else if ( m_curr_token.Get_type().equals( "RETURN" ) ) {
      Next_token(); // eat RETURN
      if ( !m_curr_token.Get_name().equals( ";" ) ) {
        // 不是';'有可能是expression
        if ( !Parse_Expression() ) return false;
      } // if

      if ( !m_curr_token.Get_name().equals( ";" ) ) {
        return false;
      } // if

      Next_token(); // eat ';'
      return true;
    } // else if

    else if ( m_curr_token.Get_type().equals( "IF" ) ) {
      Next_token(); // eat IF
      if ( !m_curr_token.Get_name().equals( "(" ) ) return false;
      Next_token(); // eat '('
      if ( !Parse_Expression() ) return false;
      if ( !m_curr_token.Get_name().equals( ")" ) ) return false;
      Next_token(); // eat ')'
      if ( !Parse_Statement() ) return false;
      if ( m_curr_token.Get_type().equals( "ELSE" ) ) {
        Next_token();  // eat ELSE
        if ( !Parse_Statement() ) return false;
      } // if

      return true;
    } // else if

    else if ( m_curr_token.Get_type().equals( "WHILE" ) ) {
      Next_token(); // eat 'WHILE'
      if ( !m_curr_token.Get_name().equals( "(" ) ) return false;
      Next_token(); // eat '('
      if ( !Parse_Expression() ) return false;
      if ( !m_curr_token.Get_name().equals( ")" ) ) return false;
      Next_token(); // eat ')'
      if ( !Parse_Statement() ) return false;
      return true;
    } // else if

    else if ( m_curr_token.Get_type().equals( "DO" ) ) {
      Next_token(); // eat DO
      if ( !Parse_Statement() ) return false;
      if ( !m_curr_token.Get_type().equals( "WHILE" ) ) return false;
      Next_token(); // eat WHILE
      if ( !m_curr_token.Get_name().equals( "(" ) ) return false;
      Next_token(); // eat '('
      if ( !Parse_Expression() ) return false;
      if ( !m_curr_token.Get_name().equals( ")" ) ) return false;
      Next_token(); // eat ')'
      if ( !m_curr_token.Get_name().equals( ";" ) ) return false;
      Next_token(); // eat ';'
      return true;
    } // else if

    else if ( m_curr_token.Get_name().equals( "{" ) ) {
      return Parse_Compound_statement();
    } // else if

    else {
      if ( !Parse_Expression() ) return false;
      if ( !m_curr_token.Get_name().equals( ";" ) ) return false;
      Next_token(); // eat ';'
      return true;
    } // else
  } // Parse_Statement()

  private boolean Parse_Expression() {
    // basic_expression { ',' basic_expression }
    if ( !Parse_Basic_expression() ) {
      return false;
    } // if

    while ( m_curr_token.Get_name().equals( "," ) ) {
      Next_token(); // eat ','
      if ( !Parse_Basic_expression() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Expression()

  private boolean Parse_Basic_expression() {
    //  Identifier rest_of_Identifier_started_basic_exp
    //  | ( PP | MM ) Identifier rest_of_PPMM_Identifier_started_basic_exp
    //  | sign { sign } signed_unary_exp romce_and_romloe
    //  | ( Constant | '(' expression ')' ) romce_and_romloe
    if ( m_curr_token.Get_type().equals( "IDENT" ) ) {
      Token ident = m_curr_token; // record for check
      // 下一個token或換行確認
      if ( m_reader.Has_next_tk() ) {
        // 有下一個token
        Next_token(); // eat IDENT
        if ( !In_table( ident ) ) {
          m_undefined_tk = ident;
          return false;
        } // if
      } // if

      else {
        // 換行
        if ( !In_table( ident ) ) {
          m_undefined_tk = ident;
          return false;
        } // if

        Next_token(); // eat IDENT
      } // else

      return Parse_Rest_of_Identifier_started_basic_exp();
    } // if()

    else if ( IsPPorMM( m_curr_token ) ) {
      Next_token(); // eat PP or MM
      if ( !m_curr_token.Get_type().equals( "IDENT" ) ) {
        return false;
      } // if()

      // 直接確認
      if ( !In_table( m_curr_token ) ) {
        m_undefined_tk = m_curr_token;
        return false;
      } // if

      Next_token(); // eat IDENT
      return Parse_Rest_of_PPMM_Identifier_started_basic_exp();
    } // else if

    else if ( IsSign_2( m_curr_token ) ) {
      while ( IsSign_2( m_curr_token ) ) {
        Next_token();
      } // while

      if ( !Parse_Signed_unary_exp() ) {
        return false;
      } // if

      return Parse_Romce_and_romloe();
    } // else if

    else if ( m_curr_token.Get_name().equals( "(" ) || m_curr_token.Get_type().equals( "CONST" ) ) {
      if ( m_curr_token.Get_name().equals( "(" ) ) {
        Next_token(); // eat '('
        if ( !Parse_Expression() ) {
          return false;
        } // if

        if ( !m_curr_token.Get_name().equals( ")" ) ) {
          return false;
        } // if

        Next_token(); // eat ')'
      } // if

      else {
        Next_token(); // eat CONST
      } // else

      return Parse_Romce_and_romloe();
    } // else if

    else {
      return false;
    } // else
  } // Parse_Basic_expression()

  private boolean Parse_Rest_of_Identifier_started_basic_exp() {
    //    [ '[' expression ']' ]
    //    ( assignment_operator basic_expression
    //      |
    //      [ PP | MM ] romce_and_romloe
    //    )
    //   | '(' [ actual_parameter_list ] ')' romce_and_romloe
    if ( m_curr_token.Get_name().equals( "[" ) ) {
      Next_token(); // eat '['
      if ( !Parse_Expression() ) {
        return false;
      } // if

      if ( !m_curr_token.Get_name().equals( "]" ) ) {
        return false;
      } // if

      Next_token(); // eat ']'
    } // if

    if ( IsAssignmentOperator( m_curr_token ) ) {
      Next_token();
      if ( !Parse_Basic_expression() ) {
        return false;
      } // if

      return true;
    } // if

    else if ( IsPPorMM( m_curr_token ) ) {
      Next_token(); // eat PP or MM
    } // else if

    else if ( m_curr_token.Get_name().equals( "(" ) ) {
      Next_token(); // eat '('
      if ( !m_curr_token.Get_name().equals( ")" ) ) {
        if ( !Parse_Actual_parameter_list() ) {
          return false;
        } // if

        if ( !m_curr_token.Get_name().equals( ")" ) ) {
          return false;
        } // if
      } // if

      Next_token(); // eat ')'
    } // else if

    return Parse_Romce_and_romloe();
  } // Parse_Rest_of_Identifier_started_basic_exp()

  private boolean Parse_Rest_of_PPMM_Identifier_started_basic_exp() {
    // [ '[' expression ']' ] romce_and_romloe
    if ( m_curr_token.Get_name().equals( "[" ) ) {
      Next_token(); // eat '['
      if ( !Parse_Expression() ) {
        return false;
      } // if

      if ( !m_curr_token.Get_name().equals( "]" ) ) {
        return false;
      } // if

      Next_token(); // eat ']'
    } // if

    return Parse_Romce_and_romloe();
  } // Parse_Rest_of_PPMM_Identifier_started_basic_exp()

  private boolean Parse_Sign() {
    // '+' | '-' | '!'
    if ( IsSign_2( m_curr_token ) ) {
      Next_token();
      return true;
    } // if

    else {
      return false;
    } // else
  } // Parse_Sign()

  private boolean Parse_Actual_parameter_list() {
    // basic_expression { ',' basic_expression }
    if ( !Parse_Basic_expression() ) {
      return false;
    } // if

    while ( m_curr_token.Get_name().equals( "," ) ) {
      Next_token(); // eat ','
      if ( !Parse_Basic_expression() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Actual_parameter_list()

  private boolean Parse_Assignment_operator() {
    // '=' | TE | DE | RE | PE | ME
    if ( IsAssignmentOperator( m_curr_token ) ) {
      Next_token();
      return true;
    } // if

    else {
      return false;
    } // else
  } // Parse_Assignment_operator()

  private boolean Parse_Romce_and_romloe() {
    // rest_of_maybe_logical_OR_exp [ '?' basic_expression ':' basic_expression ]
    if ( !Parse_Rest_of_maybe_logical_OR_exp() ) {
      return false;
    } // if

    if ( m_curr_token.Get_name().equals( "?" ) ) {
      Next_token(); // eat'?'
      if ( !Parse_Basic_expression() ) {
        return false;
      } // if

      if ( !m_curr_token.Get_name().equals( ":" ) ) {
        return false;
      } // if

      Next_token(); // eat ':'
      if ( !Parse_Basic_expression() ) {
        return false;
      } // if
    } // if

    return true;
  } // Parse_Romce_and_romloe()

  private boolean Parse_Rest_of_maybe_logical_OR_exp() {
    // rest_of_maybe_logical_AND_exp { OR maybe_logical_AND_exp }
    if ( !Parse_Rest_of_maybe_logical_AND_exp() ) {
      return false;
    } // if

    while ( m_curr_token.Get_type().equals( "OR" ) ) {
      Next_token();
      if ( !Parse_Maybe_logical_AND_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Rest_of_maybe_logical_OR_exp()

  private boolean Parse_Maybe_logical_AND_exp() {
    // maybe_bit_OR_exp { AND maybe_bit_OR_exp }
    if ( !Parse_Maybe_bit_OR_exp() ) {
      return false;
    } // if

    while ( m_curr_token.Get_type().equals( "AND" ) ) {
      Next_token();
      if ( !Parse_Maybe_bit_OR_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Maybe_logical_AND_exp()

  private boolean Parse_Rest_of_maybe_logical_AND_exp() {
    // rest_of_maybe_bit_OR_exp { AND maybe_bit_OR_exp }
    if ( !Parse_Rest_of_maybe_bit_OR_exp() ) {
      return false;
    } // if

    while ( m_curr_token.Get_type().equals( "AND" ) ) {
      Next_token();
      if ( !Parse_Maybe_bit_OR_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Rest_of_maybe_logical_AND_exp()

  private boolean Parse_Maybe_bit_OR_exp() {
    // maybe_bit_ex_OR_exp { '|' maybe_bit_ex_OR_exp }
    if ( !Parse_Maybe_bit_ex_OR_exp() ) {
      return false;
    } // if

    while ( m_curr_token.Get_name().equals( "|" ) ) {
      Next_token();
      if ( !Parse_Maybe_bit_ex_OR_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Maybe_bit_OR_exp()

  private boolean Parse_Rest_of_maybe_bit_OR_exp() {
    // rest_of_maybe_bit_ex_OR_exp { '|' maybe_bit_ex_OR_exp }
    if ( !Parse_Rest_of_maybe_bit_ex_OR_exp() ) {
      return false;
    } // if

    while ( m_curr_token.Get_name().equals( "|" ) ) {
      Next_token();
      if ( !Parse_Maybe_bit_ex_OR_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Rest_of_maybe_bit_OR_exp()

  private boolean Parse_Maybe_bit_ex_OR_exp() {
    // maybe_bit_AND_exp { '^' maybe_bit_AND_exp }
    if ( !Parse_Maybe_bit_AND_exp() ) {
      return false;
    } // if

    while ( m_curr_token.Get_name().equals( "^" ) ) {
      Next_token();
      if ( !Parse_Maybe_bit_AND_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Maybe_bit_ex_OR_exp()

  private boolean Parse_Rest_of_maybe_bit_ex_OR_exp() {
    // rest_of_maybe_bit_AND_exp { '^' maybe_bit_AND_exp }
    if ( !Parse_Rest_of_maybe_bit_AND_exp() ) {
      return false;
    } // if

    while ( m_curr_token.Get_name().equals( "^" ) ) {
      Next_token();
      if ( !Parse_Maybe_bit_AND_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Rest_of_maybe_bit_ex_OR_exp()

  private boolean Parse_Maybe_bit_AND_exp() {
    // maybe_equality_exp { '&' maybe_equality_exp }
    if ( !Parse_Maybe_equality_exp() ) {
      return false;
    } // if

    while ( m_curr_token.Get_name().equals( "&" ) ) {
      Next_token();
      if ( !Parse_Maybe_equality_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Maybe_bit_AND_exp()

  private boolean Parse_Rest_of_maybe_bit_AND_exp() {
    // rest_of_maybe_equality_exp { '&' maybe_equality_exp }
    if ( !Parse_Rest_of_maybe_equality_exp() ) {
      return false;
    } // if

    while ( m_curr_token.Get_name().equals( "&" ) ) {
      Next_token();
      if ( !Parse_Maybe_equality_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Rest_of_maybe_bit_AND_exp()

  private boolean Parse_Maybe_equality_exp() {
    // maybe_relational_exp { ( EQ | NEQ ) maybe_relational_exp}
    if ( !Parse_Maybe_relational_exp() ) {
      return false;
    } // if

    while ( IsEqualityOperator( m_curr_token ) ) {
      Next_token();
      if ( !Parse_Maybe_relational_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Maybe_equality_exp()

  private boolean Parse_Rest_of_maybe_equality_exp() {
    // rest_of_maybe_relational_exp { ( EQ | NEQ ) maybe_relational_exp }
    if ( !Parse_Rest_of_maybe_relational_exp() ) {
      return false;
    } // if

    while ( IsEqualityOperator( m_curr_token ) ) {
      Next_token();
      if ( !Parse_Maybe_relational_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Rest_of_maybe_equality_exp()

  private boolean Parse_Maybe_relational_exp() {
    // maybe_shift_exp { ( '<' | '>' | LE | GE ) maybe_shift_exp }
    if ( !Parse_Maybe_shift_exp() ) {
      return false;
    } // if

    while ( IsRelationalOperator( m_curr_token ) ) {
      Next_token();
      if ( !Parse_Maybe_shift_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Maybe_relational_exp()

  private boolean Parse_Rest_of_maybe_relational_exp() {
    // rest_of_maybe_shift_exp { ( '<' | '>' | LE | GE ) maybe_shift_exp }
    if ( !Parse_Rest_of_maybe_shift_exp() ) {
      return false;
    } // if

    while ( IsRelationalOperator( m_curr_token ) ) {
      Next_token();
      if ( !Parse_Maybe_shift_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Rest_of_maybe_relational_exp()

  private boolean Parse_Maybe_shift_exp() {
    // maybe_additive_exp { ( LS | RS ) maybe_additive_exp }
    if ( !Parse_Maybe_additive_exp() ) {
      return false;
    } // if

    while ( IsShiftOperator( m_curr_token ) ) {
      Next_token();
      if ( !Parse_Maybe_additive_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Maybe_shift_exp()

  private boolean Parse_Rest_of_maybe_shift_exp() {
    // rest_of_maybe_additive_exp { ( LS | RS ) maybe_additive_exp }
    if ( !Parse_Rest_of_maybe_additive_exp() ) {
      return false;
    } // if

    while ( IsShiftOperator( m_curr_token ) ) {
      Next_token();
      if ( !Parse_Maybe_additive_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Rest_of_maybe_shift_exp()

  private boolean Parse_Maybe_additive_exp() {
    // maybe_mult_exp { ( '+' | '-' ) maybe_mult_exp }
    if ( !Parse_Maybe_mult_exp() ) {
      return false;
    } // if

    while ( IsSign( m_curr_token ) ) {
      Next_token();
      if ( !Parse_Maybe_mult_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Maybe_additive_exp()

  private boolean Parse_Rest_of_maybe_additive_exp() {
    // rest_of_maybe_mult_exp { ( '+' | '-' ) maybe_mult_exp }
    if ( !Parse_Rest_of_maybe_mult_exp() ) {
      return false;
    } // if

    while ( IsSign( m_curr_token ) ) {
      Next_token();
      if ( !Parse_Maybe_mult_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Rest_of_maybe_additive_exp()

  private boolean Parse_Maybe_mult_exp() {
    // unary_exp rest_of_maybe_mult_exp
    if ( !Parse_Unary_exp() ) {
      return false;
    } // if

    if ( !Parse_Rest_of_maybe_mult_exp() ) {
      return false;
    } // if

    return true;
  } // Parse_Maybe_mult_exp()

  private boolean Parse_Rest_of_maybe_mult_exp() {
    // { ( '*' | '/' | '%' ) unary_exp }
    while ( IsMultiplicativeOperator( m_curr_token ) ) {
      Next_token();
      if ( !Parse_Unary_exp() ) {
        return false;
      } // if
    } // while

    return true;
  } // Parse_Rest_of_maybe_mult_exp()

  private boolean Parse_Unary_exp() {
    //  sign { sign } signed_unary_exp
    // | unsigned_unary_exp
    // | ( PP | MM ) Identifier [ '[' expression ']' ]
    if ( IsSign_2( m_curr_token ) ) {
      while ( IsSign_2( m_curr_token ) ) {
        Next_token();
      } // while

      return Parse_Signed_unary_exp();
    } // if

    else if ( IsPPorMM( m_curr_token ) ) {
      Next_token(); // eat PP or MM
      if ( !m_curr_token.Get_type().equals( "IDENT" ) ) {
        return false;
      } // if

      // 直接確認
      if ( !In_table( m_curr_token ) ) {
        m_undefined_tk = m_curr_token;
        return false;
      } // if

      Next_token();
      if ( m_curr_token.Get_name().equals( "[" ) ) {
        Next_token(); // eat '['
        if ( !Parse_Expression() ) {
          return false;
        } // if

        if ( !m_curr_token.Get_name().equals( "]" ) ) {
          return false;
        } // if

        Next_token(); // eat ']'
      } // if

      return true;
    } // else if

    else {
      return Parse_Unsigned_unary_exp();
    } // else
  } // Parse_Unary_exp()

  private boolean Parse_Signed_unary_exp() {
    // Identifier [ '(' [ actual_parameter_list ] ')'
    //              |
    //              '[' expression ']'
    //            ]
    // | Constant
    // | '(' expression ')'
    if ( m_curr_token.Get_type().equals( "IDENT" ) ) {
      Token ident = m_curr_token; // record for check
      // 下一個token或換行確認
      if ( m_reader.Has_next_tk() ) {
        // 有下一個token
        Next_token(); // eat IDENT
        if ( !In_table( ident ) ) {
          m_undefined_tk = ident;
          return false;
        } // if
      } // if

      else {
        // 換行
        if ( !In_table( ident ) ) {
          m_undefined_tk = ident;
          return false;
        } // if

        Next_token(); // eat IDENT
      } // else

      if ( m_curr_token.Get_name().equals( "(" ) ) {
        Next_token(); // eat '('
        if ( !m_curr_token.Get_name().equals( ")" ) ) {
          // 若不是'('代表後面是actual_parameter_list
          if ( !Parse_Actual_parameter_list() ) {
            return false;
          } // if
        } // if

        if ( !m_curr_token.Get_name().equals( ")" ) ) {
          return false;
        } // if

        Next_token(); // eat ')'
      } // if

      else if ( m_curr_token.Get_name().equals( "[" ) ) {
        Next_token(); // eat '['
        if ( !Parse_Expression() ) {
          return false;
        } // if

        if ( !m_curr_token.Get_name().equals( "]" ) ) {
          return false;
        } // if

        Next_token(); // eat ']'
      } // else if

      return true;
    } // if

    else if ( m_curr_token.Get_type().equals( "CONST" ) ) {
      Next_token();
      return true;
    } // else if

    else if ( m_curr_token.Get_name().equals( "(" ) ) {
      Next_token(); // eat '('
      if ( !Parse_Expression() ) {
        return false;
      } // if

      if ( !m_curr_token.Get_name().equals( ")" ) ) {
        return false;
      } // if

      Next_token(); // eat ')'
      return true;
    } // else if

    return false;
  } // Parse_Signed_unary_exp()

  private boolean Parse_Unsigned_unary_exp() {
    // Identifier [ '(' [ actual_parameter_list ] ')'
    //             |
    //             [ '[' expression ']' ] [ ( PP | MM ) ]
    //            ]
    // | Constant
    // | '(' expression ')'
    if ( m_curr_token.Get_type().equals( "IDENT" ) ) {
      Token ident = m_curr_token;
      // 下一個token或換行確認
      if ( m_reader.Has_next_tk() ) {
        // 有下一個token
        Next_token(); // eat IDENT
        if ( !In_table( ident ) ) {
          m_undefined_tk = ident;
          return false;
        } // if
      } // if

      else {
        // 換行
        if ( !In_table( ident ) ) {
          m_undefined_tk = ident;
          return false;
        } // if

        Next_token(); // eat IDENT
      } // else

      if ( m_curr_token.Get_name().equals( "(" ) ) {
        Next_token(); // eat '('
        if ( !m_curr_token.Get_name().equals( ")" ) ) {
          // 若不是'('代表後面是actual_parameter_list
          if ( !Parse_Actual_parameter_list() ) {
            return false;
          } // if
        } // if

        if ( !m_curr_token.Get_name().equals( ")" ) ) {
          return false;
        } // if

        Next_token(); // eat ')'
      } // if

      else if ( m_curr_token.Get_name().equals( "[" ) ) {
        Next_token(); // eat '['
        if ( !Parse_Expression() ) {
          return false;
        } // if

        if ( !m_curr_token.Get_name().equals( "]" ) ) {
          return false;
        } // if

        Next_token(); // eat ']'
      } // else if

      if ( IsPPorMM( m_curr_token ) ) {
        Next_token();
      } // if

      return true;
    } // if

    else if ( m_curr_token.Get_type().equals( "CONST" ) ) {
      Next_token();
      return true;
    } // else if

    else if ( m_curr_token.Get_name().equals( "(" ) ) {
      Next_token(); // eat '('
      if ( !Parse_Expression() ) {
        return false;
      } // if

      if ( !m_curr_token.Get_name().equals( ")" ) ) {
        return false;
      } // if

      Next_token(); // eat ')'
      return true;
    } // else if

    return false;
  } // Parse_Unsigned_unary_exp()

} // class Parser

class Executor{

  ArrayList<Token> m_ID_table;
  ArrayList<Token> m_Func_table;

  Executor() {
    m_ID_table = new ArrayList<Token>();
    m_Func_table = new ArrayList<Token>();
  } // Executor()

  // ================================================================================

  private boolean Is_type_specifier( String tp ) {
    if ( tp.equals( "INT" ) || tp.equals( "CHAR" ) || tp.equals( "FLOAT" ) ) return true;
    else if ( tp.equals( "STRING" ) || tp.equals( "BOOL" ) ) return true;
    return false;
  } // Is_type_specifier()

  private boolean Find_curly_bracket( ArrayList<Token> ins ) {
    // 找到大括號
    for ( int i = 0; i < ins.size() ; i++ ) {
      String name = ins.get( i ).Get_name();
      if ( name.equals( "{" ) || name.equals( "}" ) ) return true;
    } // for

    return false;
  } // Find_curly_bracket()

  private String Get_ins_type( ArrayList<Token> ins ) {

    String tp = "";
    if ( ins.size() > 0 ) tp = ins.get( 0 ).Get_type();

    if ( Find_curly_bracket( ins ) && ( Is_type_specifier( tp ) || tp.equals( "VOID" ) ) ) return "func_def";
    else if ( Is_type_specifier( tp ) ) return "def";
    else return "stat";
  } // Get_ins_type()

  private void Add_ident( Token tk ) {
    m_ID_table.add( tk );
  } // Add_ident()

  private void Add_func( Token tk ) {
    m_Func_table.add( tk );
  } // Add_func()

  private boolean In_ID_table( Token tk ) {
    String name = tk.Get_name();
    for ( int i = 0; i < m_ID_table.size() ; i++ ) {
      if ( m_ID_table.get( i ).Get_name().equals( name ) ) return true;
    } // for

    return false;
  } // In_ID_table()

  private boolean In_func_table( Token tk ) {
    String name = tk.Get_name();
    for ( int i = 0; i < m_Func_table.size() ; i++ ) {
      if ( m_Func_table.get( i ).Get_name().equals( name ) ) return true;
    } // for

    return false;
  } // In_func_table()

  private String Get_ID_def_ins( ArrayList<Token> ins, int idxOfVar ) {
    // string str, str1 ;
    // string str3 ;
    // char chArray[30] ;
    String ret = ins.get( 0 ).Get_name() + " "; // type;
    if ( Is_Array_def( ins ) ) {
      ret = ret + ins.get( idxOfVar ).Get_name();
      ret = ret + ins.get( idxOfVar+1 ).Get_name() + " ";
      ret = ret + ins.get( idxOfVar+2 ).Get_name() + " ";
      ret = ret + ins.get( idxOfVar+3 ).Get_name();
    } // if

    else {
      ret = ret + ins.get( idxOfVar ).Get_name();
    } // else

    ret = ret + " ;";
    return ret;
  } // Get_ID_def_ins()

  private ArrayList<Token> Get_def_tk( ArrayList<Token> ins ) {
    // 回傳要定義的變數們、設定變數宣告訊息
    ArrayList<Token> ret = new ArrayList<Token>();
    if ( Is_Array_def( ins ) ) {
      // 定義array
      for ( int i = 1; i < ins.size() ; i = i + 5 ) {

        Token tmp = ins.get( i );
        if ( In_ID_table( tmp ) ) {
          tmp = m_ID_table.get( Get_ID_index( tmp.Get_name() ) ); // 更新到真正的IDENT
        } // if

        String def = Get_ID_def_ins( ins, i );
        tmp.Set_def_ins( def );
        ret.add( tmp );
      } // for
    } // if

    else {
      // 一般定義
      for ( int i = 1; i < ins.size() ; i = i + 2 ) {

        Token tmp = ins.get( i );
        if ( In_ID_table( tmp ) ) {
          tmp = m_ID_table.get( Get_ID_index( tmp.Get_name() ) ); // 更新到真正的IDENT
        } // if

        String def = Get_ID_def_ins( ins, i );
        tmp.Set_def_ins( def );
        ret.add( tmp );
      } // for
    } // else

    return ret;
  } // Get_def_tk()

  private boolean Is_Array_def( ArrayList<Token> ins ) {

    for ( int i = 0; i < ins.size() ; i++ ) {
      String name = ins.get( i ).Get_name();
      if ( name.equals( "[" ) || name.equals( "]" ) ) return true;
    } // for

    return false;
  } // Is_Array_def()

  private String Get_Stat_method( ArrayList<Token> ins ) {
    // 檢查內建function call
    for ( int i = 0; i < ins.size() ; i++ ) {
      String n = ins.get( i ).Get_name();
      if ( n.equals( "ListAllVariables" ) ) return "LAV";
      else if ( n.equals( "ListVariable" ) ) return "LV";
      else if ( n.equals( "ListAllFunctions" ) ) return "LAF";
      else if ( n.equals( "ListFunction" ) ) return "LF";
      else if ( n.equals( "cout" ) ) return "COUT";
    } // for

    return "NONE";
  } // Get_Stat_method()

  private void Sort_ID_table() {

    int n = m_ID_table.size();
    for ( int i = 0; i < n-1 ; i++ ) {
      for ( int j = 0; j < n-i-1 ; j++ ) {
        String id1 = m_ID_table.get( j ).Get_name();
        String id2 = m_ID_table.get( j+1 ).Get_name();
        if ( id1.compareTo( id2 ) > 0 ) {
          // id1 > id2
          Token tk1 = m_ID_table.get( j );
          Token tk2 = m_ID_table.get( j+1 );
          m_ID_table.set( j+1, tk1 );
          m_ID_table.set( j, tk2 );
        } // if
      } // for
    } // for
  } // Sort_ID_table()

  private void Sort_func_table() {

    int n = m_Func_table.size();
    for ( int i = 0; i < n-1 ; i++ ) {
      for ( int j = 0; j < n-i-1 ; j++ ) {
        String id1 = m_Func_table.get( j ).Get_name();
        String id2 = m_Func_table.get( j+1 ).Get_name();
        if ( id1.compareTo( id2 ) > 0 ) {
          // id1 > id2
          Token tk1 = m_Func_table.get( j );
          Token tk2 = m_Func_table.get( j+1 );
          m_Func_table.set( j+1, tk1 );
          m_Func_table.set( j, tk2 );
        } // if
      } // for
    } // for
  } // Sort_func_table()

  private void Print_ID_table() {

    for ( int i = 0; i < m_ID_table.size() ; i++ ) {
      String tk_name = m_ID_table.get( i ).Get_name();
      System.out.println( tk_name );
    } // for
  } // Print_ID_table()

  private void Print_func_table() {

    for ( int i = 0; i < m_Func_table.size() ; i++ ) {
      String tk_name = m_Func_table.get( i ).Get_name();
      System.out.println( tk_name + "()" );
    } // for
  } // Print_func_table()

  private int Get_ID_index( String str ) {
    for ( int i = 0; i < m_ID_table.size() ; i++ ) {
      if ( m_ID_table.get( i ).Get_name().equals( str ) ) return i;
    } // for

    return -1;
  } // Get_ID_index()

  private int Get_func_index( String str ) {
    for ( int i = 0; i < m_Func_table.size() ; i++ ) {
      if ( m_Func_table.get( i ).Get_name().equals( str ) ) return i;
    } // for

    return -1;
  } // Get_func_index()

  private String Add_space( String str, int n ) {
    n = n * 2;
    for ( int i = n ; i > 0 ; i-- ) {
      str = str + " ";
    } // for

    return str;
  } // Add_space()

  private boolean Need_space( ArrayList<Token> ins, int idx ) {
    // 不需要補空白
    String str = ins.get( idx ).Get_name();
    if ( str.equals( "[" ) ) return false;
    if ( str.equals( "}" ) ) return false;
    if ( str.equals( "++" ) ) return false;
    if ( str.equals( "--" ) ) return false;
    if ( str.equals( "," ) ) return false;
    if ( idx == 2 ) return false;
    if ( idx > 1 && In_func_table( ins.get( idx - 1 ) ) ) return false;
    return true;
  } // Need_space()

  private String Get_func_content( ArrayList<Token> ins ) {
    // 處理function內容輸出
    String ret = "";
    int level = 0; // 縮排
    boolean new_line = false;
    boolean first = true;
    // 你在搞我 ????
    boolean m_IF_WHILE = false;
    boolean only_one_line = false;
    boolean last_is_bracket = false;
    for ( int i = 0; i < ins.size() ; i++ ) {

      String n = ins.get( i ).Get_name();

      if ( n.equals( "{" ) ) level = level + 1;
      if ( n.equals( "}" ) ) level = level - 1;

      if ( new_line ) {
        // 剛換行，要補空格
        new_line = false;
        ret = ret + "\n";
        ret = Add_space( ret, level );
      } // if

      else if ( only_one_line ) {
        // 剛換行，要補空格
        only_one_line = false;
        ret = ret + "\n";
        ret = Add_space( ret, level );
        level = level - 1;
      } // else if

      else {
        if ( Need_space( ins, i ) ) {
          ret = ret + " ";
        } // if

        if ( first ) {
          ret = ret.substring( 0, ret.length()-1 );
          first = false;
        } // if

        if ( last_is_bracket && n.equals( ")" ) ) {
          ret = ret.substring( 0, ret.length()-1 ); // 括號內沒有東西
        } // if
      } // else

      last_is_bracket = false;

      if ( n.equals( ")" ) && m_IF_WHILE && !ins.get( i+1 ).Get_name().equals( "{" ) ) {
        // 沒有 "{" 跟 "}"
        m_IF_WHILE = false;
        only_one_line = true;
        level = level + 1;
      } // if

      if ( n.equals( "else" ) && !ins.get( i+1 ).Get_name().equals( "{" ) ) {
        // 沒有 "{" 跟 "}"
        only_one_line = true;
        level = level + 1;
      } // if

      if ( n.equals( "if" ) || n.equals( "while" ) ) m_IF_WHILE = true;

      if ( n.equals( "(" ) ) last_is_bracket = true;

      if ( n.equals( ";" ) || n.equals( "{" ) || n.equals( "}" ) ) {
        // 該換行了
        new_line = true;
      } // if

      ret = ret + n;
    } // for

    return ret;
  } // Get_func_content()

  private int get_weight( String str ) {
  // 返回權重
    if ( str.equals( "*" ) || str.equals( "/" ) ) return 10;
    if ( str.equals( "+" ) || str.equals( "-" ) ) return 5;
    else return 1;
  }

  private boolean is_operator( String str ) {
    if ( str.equals( "+" ) || str.equals( "-" ) || str.equals( "*" ) || str.equals( "/" ) ) return true;
    return false;
  }

  private String do_arith( String s1, String s2, String s3 ) {
    if ( s1.equals( "+" ) ) return Double.toString(Double.parseDouble(s2) + Double.parseDouble(s3));
    if ( s1.equals( "-" ) ) return Double.toString(Double.parseDouble(s2) - Double.parseDouble(s3));
    if ( s1.equals( "*" ) ) return Double.toString(Double.parseDouble(s2) * Double.parseDouble(s3));
    if ( s1.equals( "/" ) ) return Double.toString(Double.parseDouble(s2) / Double.parseDouble(s3));
    return "";
  }

  private ArrayList<Token> toRPN( ArrayList<Token> expr ) {
  // 逆波蘭表示法
    Stack<Token> s = new Stack<Token>();
    ArrayList<Token> postfix = new ArrayList<Token>();

    for ( Token tk : expr ) {
      if ( is_operator( tk.Get_name() ) ) {
        while ( !s.isEmpty() && get_weight( s.peek().Get_name() ) >= get_weight( tk.Get_name() ) ) {
        // 權重比stack中的小，拿出來
          postfix.add( s.pop() );
        }

        // 放得進去了，把tk放入stack
        s.push( tk );
      }

      else if ( tk.Get_name().equals( "(" ) ) {
      // 直接丟到s中
        s.push( tk );
      }

      else if ( tk.Get_name().equals( ")" ) ) {
      // pop直到遇到"("
        while ( !s.isEmpty() && !s.peek().Get_name().equals( "(" ) ) {
          postfix.add( s.pop() );
        }

        s.pop(); // 把"("丟了
      }

      else {
      // 數字直接丟進去
        postfix.add( tk );
      }
    }

    // 把剩下的全部pop出來
    while ( !s.isEmpty() ) {
      postfix.add( s.pop() );
    }

    return postfix;
  }

  private String Evaluate_postfix( ArrayList<Token> postfix ) {
    Stack<Token> s = new Stack<Token>();

    for ( Token tk : postfix ) {
      String str = tk.Get_name();
      if ( is_operator( str ) ) {
      // 遇到operator要計算，注意順序 !!!!
        String s1 = s.pop().Get_name();
        String s2 = s.pop().Get_name();
        Token new_tk = new Token();
        new_tk.Set_name( do_arith( str, s2, s1 ) );
        s.push( new_tk );
      }

      else {
        s.push( tk );
      }
    }

    return s.pop().Get_name();
  }

  public void Execute_ins( ArrayList<Token> ins ) {

    String tp = Get_ins_type( ins );
    System.out.print( "> " );
    if ( tp.equals( "def" ) ) {
      // 定義IDENT或是array
      ArrayList<Token> def_tk = Get_def_tk( ins );
      for ( int i = 0; i < def_tk.size() ; i++ ) {
        Token tk = def_tk.get( i );
        if ( !In_ID_table( tk ) ) {
          // 第一次define
          Add_ident( tk );
          System.out.println( "Definition of " + tk.Get_name() + " entered ..." );
        } // if

        else {
          // 重複define
          System.out.println( "New definition of " + tk.Get_name() + " entered ..." );
        } // else
      } // for
    } // if

    else if ( tp.equals( "func_def" ) ) {
      // 定義function
      Token tk = new Token();
      tk.Set_name( ins.get( 1 ).Get_name() );
      String def_ins = Get_func_content( ins );
      tk.Set_def_ins( def_ins );

      if ( !In_func_table( tk ) ) {
        // 第一次define
        Add_func( tk );
        System.out.println( "Definition of " + tk.Get_name() + "() entered ..." );
      } // if

      else {
        // 重複define，需要更新定義
        int idx = Get_func_index( tk.Get_name() );
        if ( idx != -1 ) tk = m_Func_table.get( idx );
        tk.Set_def_ins( def_ins ); // 更新function內容
        System.out.println( "New definition of " + tk.Get_name() + "() entered ..." );
      } // else
    } // else if

    else {
      // statement
      String stat_tp = Get_Stat_method( ins );

      if ( stat_tp.equals( "LAV" ) ) {
        Sort_ID_table();
        Print_ID_table();
      } // if

      else if ( stat_tp.equals( "LV" ) ) {
        String  target = ins.get( 2 ).Get_name();
        if ( target.equals( "(" ) ) target = ins.get( 3 ).Get_name(); // 暴力解法?????
        target = target.replace( "\"", "" ); // 處理掉 "\""
        int idx = Get_ID_index( target );
        if ( idx != -1 ) System.out.println( m_ID_table.get( idx ).Get_def_ins() );
      } // else if

      else if ( stat_tp.equals( "LAF" ) ) {
        Sort_func_table();
        Print_func_table();
      } // else if

      else if ( stat_tp.equals( "LF" ) ) {
        String  target = ins.get( 2 ).Get_name();
        if ( target.equals( "(" ) ) target = ins.get( 3 ).Get_name(); // 暴力解法?????
        target = target.replace( "\"", "" ); // 處理掉 "\""
        int idx = Get_func_index( target );
        if ( idx != -1 ) System.out.println( m_Func_table.get( idx ).Get_def_ins() );
      } // else if

      else if ( stat_tp.equals( "COUT" ) ) {
      // cout
        int idx = 2; // skip "cout" 、 "<<"
        ArrayList<Token> expr = new ArrayList<Token>();
        while ( idx < ins.size() ) {
          String tk_name = ins.get(idx).Get_name();
          if ( tk_name.equals( "<<" ) || tk_name.equals( ">>" ) ) {
          // 一個expression完結，做運算
            ArrayList<Token> postfix = toRPN( expr );
            System.out.println( Evaluate_postfix( postfix ) );
            expr.clear();
          }

          else {
            expr.add( ins.get( idx ) );
          }

          idx = idx + 1;
        }

        // 最後一run
        expr.remove( expr.size() - 1 ); // 去掉";"
        ArrayList<Token> postfix = toRPN( expr );
        System.out.println( Evaluate_postfix( postfix ) );
        expr.clear();
      }

      System.out.println( "Statement executed ..." );
    } // else
  } // Execute_ins()
} // class Executor

class Main {
  public static void main( String[] args ) {

    Scanner sc = new Scanner( System.in );
    String trash = sc.nextLine(); // 題號

    Reader reader = new Reader( sc );
    Executor executor = new Executor();
    Parser parser = new Parser( reader, executor.m_ID_table, executor.m_Func_table );
    boolean run = true;

    System.out.println( "Our-C running ..." );

    while ( run ) {

      if ( parser.Parse_User_input() ) {
        if ( parser.First_is_Done() ) {
          run = false;
        } // if

        // 輸出delay一個回合
        ArrayList<Token> ins = parser.Get_instruction();
        ins.remove( ins.size() - 1 ); // 多解析了一個token

        if ( ins.size() > 0 ) executor.Execute_ins( ins ); // 執行指令

        reader.m_last_correct_line = reader.m_last_tk_line;
        reader.m_in_zhujie = false;
        parser.Give_back();
        parser.m_is_func_def = false;
      } // if

      else {
        // 處理錯誤
        int error_line = reader.Get_error_line();
        System.out.print( "> Line " + error_line + " : " );
        if ( parser.m_unrecognized_tk != null ) {
          char ch = parser.m_unrecognized_tk.Get_name().charAt( 0 );
          System.out.println( "unrecognized token with first char : \'" + ch + "\'" );
        } // if

        else if ( parser.m_unexpected_tk != null ) {
          System.out.println( "unexpected token : \'" + parser.m_unexpected_tk.Get_name() + "\'" );
        } // else if

        else {
          System.out.println( "undefined identifier : \'" + parser.m_undefined_tk.Get_name() + "\'" );
        } // else

        reader.Skip_line();
        parser.Reset_variable();
        parser.Clear_instruction();
        // 發生連續error要更新上一行的line number，因為error時不會多抓一個token，所以行數是正確的
        reader.m_last_correct_line = reader.m_num_of_line;
        reader.m_in_zhujie = false;
      } // else
    } // while

    System.out.println( "> Our-C exited ..." );
  } // main()

  static private void Print_ins( ArrayList<Token> ins ) {
    for ( int i = 0; i < ins.size() ; i++ ) {
      String name = ins.get( i ).Get_name();
      System.out.print( name + "  " );
    } // for

    System.out.println();
  } // Print_ins()
} // class Main