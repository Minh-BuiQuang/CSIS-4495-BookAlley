namespace BookAlleyWebApi.RestModels
{
    public class CreatePostRequest
    {
        public string? Author { get; set; }
        public string? ISBN { get; set; }
        public required string BookTitle { get; set; }
        public string? Image { get; set; }
        public required string Location { get; set; }
        public string? Note { get; set; }

        public required DateTimeOffset DatePosted { get; set; }
    }
}
