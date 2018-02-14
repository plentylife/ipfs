package life.plenty.model.octopi

trait Question extends Space with WithParent[Space] {}

class BasicQuestion() extends Question {}

class ContainerSpace() extends Space with WithMembers with WithParent[Space] {
}