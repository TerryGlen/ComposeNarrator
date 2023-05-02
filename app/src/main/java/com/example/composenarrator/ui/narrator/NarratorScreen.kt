package com.example.composenarrator.ui.narrator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composenarrator.data.sampleText
import com.example.composenarrator.ui.components.CustomClickableText
import com.example.composenarrator.ui.theme.ComposeNarratorTheme
import kotlinx.coroutines.launch


@Composable
fun NarratorScreen(modifier: Modifier = Modifier, viewModel: NarratorViewModel = hiltViewModel()) {
    val state = viewModel.state
    NarratorScreen(state, modifier, viewModel::onEvent )
}

@Composable
fun NarratorScreen(
    state: NarratorScreenState,
    modifier: Modifier = Modifier,
    onTtsEvent: (NarratorEvent) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    Surface {
        Column(
            modifier = modifier
                .fillMaxSize(),
        ) {
            val annotatedString = buildAnnotatedString {
                append(state.text)
                addStyle(
                    SpanStyle(
                        background = MaterialTheme.colorScheme.primary, color = contentColorFor(
                            backgroundColor = MaterialTheme.colorScheme.primary
                        )
                    ), state.ttsStartIndex, state.ttsStopIndex
                )
            }
            CustomClickableText(
                text = annotatedString,
                Modifier
                    .padding(horizontal = 18.dp)
                    .padding(top = 16.dp)
                    .weight(.9f)
                    .verticalScroll(scrollState)
                    .testTag("narration_text"),
                onClick = { onTtsEvent(NarratorEvent.Restart(it)) },
                style = MaterialTheme.typography.bodyLarge,
                onTextLayout = {
                    val currentLine = it.getLineForOffset(state.ttsStartIndex)
                    val linePosition = it.getLineBottom(currentLine)
                    if (scrollState.maxValue < linePosition) {
                        scope.launch {
                            scrollState.animateScrollTo(linePosition.toInt())
                        }
                    }
                    if (scrollState.value > linePosition) {
                        scope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    }
                }
            )
            AudioControllerBottomBar(
                state = state,
                handleTtsEvent = onTtsEvent,
                modifier = Modifier.weight(.1f)
            )
        }
    }
}

@Composable
fun AudioControllerBottomBar(state: NarratorScreenState, modifier: Modifier = Modifier, handleTtsEvent: (NarratorEvent) -> Unit = {}){
    var expanded by remember{ mutableStateOf(false)}
    Surface(color = MaterialTheme.colorScheme.secondaryContainer){
        Column(modifier) {
            if(expanded){
                TtsSliders(state = state, onTtsEvent = handleTtsEvent )
            }
            Row (
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = {handleTtsEvent(NarratorEvent.Stop)}) {
                    Icon(Icons.Default.Stop, null)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.SkipPrevious, null)
                }
                IconButton(onClick = { handleTtsEvent(NarratorEvent.Rewind)}) {
                    Icon(Icons.Default.FastRewind, null)
                }
                if (!state.ttsRunning){
                    IconButton(onClick = {handleTtsEvent(NarratorEvent.Start)}) {
                        Icon(Icons.Default.PlayArrow, null)
                    }
                } else {
                    IconButton(onClick = {handleTtsEvent(NarratorEvent.Pause)}) {
                        Icon(Icons.Default.Pause, null)
                    }
                }
                IconButton(onClick = { handleTtsEvent(NarratorEvent.FastForward)  }) {
                    Icon(Icons.Default.FastForward, null)
                }
                IconButton(onClick = {  }) {
                    Icon(Icons.Default.SkipNext, null)
                }
                IconButton(onClick = { expanded = !expanded }) {
                    val arrowIcon = if(expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp
                    Icon(arrowIcon, null)
                }
            }
        }
    }
}

@Composable
fun TtsSliders(state: NarratorScreenState, onTtsEvent: (NarratorEvent) -> Unit){
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically){
            Text("Pitch",
                Modifier
                    .padding(horizontal = 8.dp)
                    .weight(.2f), style = MaterialTheme.typography.labelMedium)
            Slider(value = state.ttsPitch,
                valueRange = 0f..2f,
                onValueChange = { onTtsEvent(NarratorEvent.UpdateTtsPitch(it)) },
                onValueChangeFinished = { onTtsEvent(NarratorEvent.Restart()) },
                modifier = Modifier.weight(.8f)
            )
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically){
            Text("Rate",
                Modifier
                    .padding(horizontal = 8.dp)
                    .weight(.2f), style = MaterialTheme.typography.labelMedium)
            Slider(value = state.ttsSpeakRate,
                valueRange = 0f..2f,
                onValueChange = { onTtsEvent(NarratorEvent.UpdateTtsSpeakRate(it)) },
                onValueChangeFinished = { onTtsEvent(NarratorEvent.Restart()) },
                modifier = Modifier.weight(.8f)
            )
        }
    }
}

@Preview
@Composable
private fun AudioControllerBottomBarPreview(){
    ComposeNarratorTheme {
        AudioControllerBottomBar(state = NarratorScreenState())
    }
}

@Preview
@Composable
private fun PreviewSampleTtsScreen(){
    val state = remember { NarratorScreenState(text = sampleText) }
    ComposeNarratorTheme {
            NarratorScreen(state)
    }
}