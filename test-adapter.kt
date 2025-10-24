import com.coleleavitt.logos.intellij.LogosLexerAdapter
import java.io.File

fun main() {
    val file = File("test-compliance.x")
    val text = file.readText()

    println("File size: ${text.length}")

    val lexer = LogosLexerAdapter()
    lexer.start(text, 0, text.length, 0)

    var segmentCount = 0
    var lastEnd = 0

    // Simulate what IntelliJ does
    while (true) {
        val tokenType = lexer.tokenType
        if (tokenType == null) break

        val start = lexer.tokenStart
        val end = lexer.tokenEnd

        if (segmentCount < 5 || segmentCount % 100 == 0) {
            println("Segment $segmentCount: start=$start, end=$end, type=$tokenType")
        }

        lastEnd = end
        segmentCount++
        lexer.advance()
    }

    println("\nTotal segments: $segmentCount")
    println("Last segment end: $lastEnd")
    println("Text length: ${text.length}")
    println("Match: ${lastEnd == text.length}")

    // This is IntelliJ's check
    if (text.length > 0 && lastEnd != text.length) {
        println("\n❌ FAIL: Unexpected termination offset for lexer")
        println("   Expected: ${text.length}")
        println("   Got: $lastEnd")
        println("   Gap: ${text.length - lastEnd} characters")
    } else {
        println("\n✅ PASS")
    }
}
